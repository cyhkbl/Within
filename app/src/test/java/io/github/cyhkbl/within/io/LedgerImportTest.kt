package io.github.cyhkbl.within.io

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 用一份真实的钱迹导出文件（510 条）跑解析，确保：
 * - xlsx 能直接读；
 * - 四种类型都认得出来；
 * - 转账带着两个账户；
 * - 退款带着关联账单；
 * - 金额按「分」精确落库。
 */
class LedgerImportTest {

    private val sample: ByteArray by lazy {
        javaClass.classLoader!!.getResourceAsStream("qianji-sample.xlsx")!!.readBytes()
    }

    @Test
    fun `parses qianji xlsx without conversion`() {
        val outcome = LedgerImport.parse(sample, "qianji.xlsx")

        assertEquals(ImportFormat.XLSX, outcome.format)
        assertEquals(510, outcome.dataLineCount)
        assertEquals(510, outcome.rows.size)
        assertTrue("不应该有解析失败的行: ${outcome.issues}", outcome.issues.isEmpty())
    }

    @Test
    fun `classifies all four kinds`() {
        val outcome = LedgerImport.parse(sample, "qianji.xlsx")
        val byKind = outcome.rows.groupingBy { it.kind }.eachCount()

        assertEquals(322, byKind["EXPENSE"])
        assertEquals(171, byKind["INCOME"])
        assertEquals(16, byKind["TRANSFER"])
        assertEquals(1, byKind["REFUND"])
    }

    @Test
    fun `transfer carries both accounts`() {
        val outcome = LedgerImport.parse(sample, "qianji.xlsx")
        val transfers = outcome.rows.filter { it.kind == "TRANSFER" }

        assertEquals(16, transfers.size)
        assertTrue(
            "每笔转账都要有转出和转入账户",
            transfers.all { it.account.isNotBlank() && it.toAccount.isNotBlank() }
        )
        assertEquals(
            "转账总额应与钱迹一致（12235.15）",
            1_223_515L,
            transfers.sumOf { it.amount }
        )
    }

    @Test
    fun `refund keeps link to the original entry`() {
        val outcome = LedgerImport.parse(sample, "qianji.xlsx")
        val refund = outcome.rows.single { it.kind == "REFUND" }

        assertEquals(990L, refund.amount)                 // 9.90
        assertEquals("支付宝", refund.account)
        assertEquals("qj1772964348405157388", refund.linkedExternalId)
        assertEquals(
            LocalDateTime.of(2026, 3, 23, 15, 50, 26),
            refund.clock
        )
    }

    @Test
    fun `keeps seconds in timestamps`() {
        val outcome = LedgerImport.parse(sample, "qianji.xlsx")
        val first = outcome.rows.first()
        assertEquals(
            LocalDateTime.parse("2026-11-10 12:24:53", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            first.clock
        )
    }

    @Test
    fun `detects duplicates by fingerprint shape`() {
        val outcome = LedgerImport.parse(sample, "qianji.xlsx")
        val first = outcome.rows.first()
        // 指纹 = 时间 + 金额 + 账户 + 类型，同一行解析两遍必须得到同样的值
        val second = LedgerImport.parse(sample, "qianji.xlsx").rows.first()
        assertEquals(first.timestampKey(), second.timestampKey())
        assertEquals(first.amount, second.amount)
        assertEquals(first.account, second.account)
    }

    @Test
    fun `rejects unknown type instead of silently treating as expense`() {
        val csv = """
            ID,时间,类型,金额,分类,账户1
            x1,2026-01-01 10:00:00,飞行,5.00,其它,现金
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "bad.csv")
        assertEquals(0, outcome.rows.size)
        assertEquals(1, outcome.issues.size)
        assertTrue(outcome.issues.first().reason.contains("未知的类型"))
    }

    @Test
    fun `rejects unparsable amount with line number`() {
        val csv = """
            ID,时间,类型,金额,分类,账户1
            x1,2026-01-01 10:00:00,支出,abc,其它,现金
            x2,2026-01-01 11:00:00,支出,12.34,三餐,现金
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "bad.csv")
        assertEquals(1, outcome.rows.size)
        assertEquals(1, outcome.issues.size)
        assertEquals(2, outcome.issues.first().lineNo)
    }

    @Test
    fun `normalises category aliases`() {
        assertEquals("汽车加油", LedgerImport.normalizeCategory("汽车/加油"))
        assertEquals("交通", LedgerImport.normalizeCategory("交通出行"))
        assertEquals("发红包", LedgerImport.normalizeCategory("转账红包"))
        assertEquals("自定义分类", LedgerImport.normalizeCategory("自定义分类"))
    }

    @Test
    fun `reads plain csv export we write ourselves`() {
        val csv = """
            时间,类型,金额,分类,账户,备注,标签,币种,手续费
            2026-01-01 10:00:00,支出,12.34,三餐,现金,午饭,,CNY,
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "exports.csv")
        assertEquals(ImportFormat.CSV, outcome.format)
        assertEquals(1, outcome.rows.size)
        assertEquals(1234L, outcome.rows.first().amount)
        assertEquals("EXPENSE", outcome.rows.first().kind)
    }

    @Test
    fun `nests sub category as parent slash child`() {
        val csv = """
            时间,类型,金额,分类,二级分类,账户1
            2026-01-01 10:00:00,支出,8.00,交通,公交,现金
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "sub.csv")
        assertEquals("交通/公交", outcome.rows.first().category)
    }

    @Test
    fun `marks reimbursable entries`() {
        val csv = """
            时间,类型,金额,分类,账户1,已报销
            2026-01-01 10:00:00,支出,80.00,请客送礼,现金,是
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "reimb.csv")
        assertTrue(outcome.rows.first().reimbursable)
    }
    @Test
    fun `transfer survives export and reimport round trip`() {
        val csv = """
            时间,类型,金额,分类,账户,备注,标签,币种,手续费
            2026-01-01 10:00:00,转账,100.00,,支付宝 > 招商银行,,,,CNY,0
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "roundtrip.csv")
        val row = outcome.rows.single()
        assertEquals("TRANSFER", row.kind)
        assertEquals("支付宝", row.account)
        assertEquals("招商银行", row.toAccount)
        assertEquals(10000L, row.amount)
    }

    @Test
    fun `same amount and second but distinct ids are both parsed`() {
        val csv = """
            ID,时间,类型,金额,分类,账户1
            qj1,2025-10-17 17:56:40,支出,100.00,三餐,支付宝
            qj2,2025-10-17 17:56:40,支出,100.00,三餐,支付宝
        """.trimIndent().toByteArray()

        val outcome = LedgerImport.parse(csv, "twin.csv")
        assertEquals(2, outcome.rows.size)
        assertEquals(listOf("qj1", "qj2"), outcome.rows.map { it.externalId })
    }
    private fun ParsedRow.timestampKey(): String = clock.toString()
}
