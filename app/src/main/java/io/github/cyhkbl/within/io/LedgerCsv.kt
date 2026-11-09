package io.github.cyhkbl.within.io

import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.util.DateUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ImportRow(
    val clock: LocalDateTime,
    val kind: String,
    val amountCents: Long,
    val categoryName: String,
    val accountName: String,
    val note: String,
    val tagText: String
)

data class ImportResult(
    val imported: Int,
    val skipped: Int,
    val messages: List<String>
)

/**
 * 钱迹兼容的 CSV 读写。
 *
 * 导出列顺序按钱迹的习惯：时间,类型,金额,分类,账户,备注,标签,币种,手续费。
 * 导入时不依赖列顺序，按表头名字匹配，所以别的记账 App 导出的表也能读进去。
 */
object LedgerCsv {

    private const val HEADER = "时间,类型,金额,分类,账户,备注,标签,币种,手续费"

    private val timeFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    fun buildCsv(
        transactions: List<TransactionEntity>,
        categoryNames: Map<Long, String>,
        accountNames: Map<Long, String>
    ): String {
        val sb = StringBuilder()
        sb.append(HEADER).append('\n')
        transactions.sortedBy { it.timestamp }.forEach { tx ->
            val typeLabel = when (tx.kind) {
                "INCOME" -> "收入"
                "TRANSFER" -> "转账"
                else -> "支出"
            }
            val time = DateUtils.toLocalDateTime(tx.timestamp)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            sb.append(
                CsvUtils.row(
                    time,
                    typeLabel,
                    io.github.cyhkbl.within.util.MoneyUtils.formatPlain(tx.amount),
                    categoryNames[tx.categoryId] ?: "",
                    accountNames[tx.accountId] ?: "",
                    tx.note,
                    tx.tags,
                    "CNY",
                    io.github.cyhkbl.within.util.MoneyUtils.formatPlain(tx.fee)
                )
            ).append('\n')
        }
        return sb.toString()
    }

    fun parseCsv(text: String): List<ImportRow> {
        val rows = CsvUtils.parse(text)
        if (rows.isEmpty()) return emptyList()

        val header = rows.first().map { it.trim() }
        fun col(vararg names: String): Int =
            header.indexOfFirst { h -> names.any { n -> h.equals(n, ignoreCase = true) || h.contains(n, ignoreCase = true) } }

        val timeIdx = col("时间", "日期", "time", "date")
        val typeIdx = col("类型", "type")
        val amountIdx = col("金额", "amount", "money")
        val categoryIdx = col("分类", "category")
        val accountIdx = col("账户", "资产", "account")
        val noteIdx = col("备注", "note", "remark", "description")
        val tagIdx = col("标签", "tag")

        if (amountIdx < 0) return emptyList()

        val out = ArrayList<ImportRow>()
        for (i in 1 until rows.size) {
            val cells = rows[i]
            fun cell(idx: Int): String = if (idx in cells.indices) cells[idx].trim() else ""
            val amount = parseAmount(cell(amountIdx)) ?: continue
            val clock = parseTime(cell(timeIdx)) ?: LocalDateTime.now()
            val rawType = cell(typeIdx)
            val kind = when {
                rawType.contains("收入") || rawType.equals("INCOME", true) || rawType.contains("income") -> "INCOME"
                rawType.contains("转账") || rawType.equals("TRANSFER", true) -> "TRANSFER"
                else -> "EXPENSE"
            }
            out.add(
                ImportRow(
                    clock = clock,
                    kind = kind,
                    amountCents = amount,
                    categoryName = cell(categoryIdx),
                    accountName = cell(accountIdx),
                    note = cell(noteIdx),
                    tagText = cell(tagIdx)
                )
            )
        }
        return out
    }

    suspend fun importInto(repo: LedgerRepository, rows: List<ImportRow>): ImportResult {
        if (rows.isEmpty()) return ImportResult(0, 0, listOf("没有可导入的行"))

        val categories = ArrayList<CategoryEntity>()
        val accounts = ArrayList<AccountEntity>()
        // 预先缓存一份，避免逐行查询
        var messages = ArrayList<String>()
        var imported = 0
        var skipped = 0

        for (row in rows) {
            try {
                val accName = row.accountName.ifBlank { "现金" }
                val acc = findOrCreateAccount(repo, accName)
                val catId = if (row.categoryName.isBlank()) null
                else findOrCreateCategory(repo, row.categoryName, row.kind)?.id
                repo.insertTransaction(
                    TransactionEntity(
                        kind = row.kind,
                        amount = row.amountCents,
                        categoryId = catId,
                        accountId = acc.id,
                        note = row.note,
                        tags = row.tagText,
                        timestamp = DateUtils.fromLocalDateTime(row.clock),
                        source = "IMPORT"
                    )
                )
                imported++
            } catch (t: Throwable) {
                skipped++
                if (messages.size < 3) messages.add("跳过一行：${t.message ?: "未知错误"}")
            }
        }
        return ImportResult(imported, skipped, messages)
    }

    private suspend fun findOrCreateAccount(repo: LedgerRepository, name: String): AccountEntity {
        val existing = repo.accountByName(name)
        if (existing != null) return existing
        val id = repo.saveAccount(AccountEntity(name = name, type = "WALLET", sortOrder = 99))
        return repo.accountById(id) ?: AccountEntity(id = id, name = name)
    }

    private suspend fun findOrCreateCategory(
        repo: LedgerRepository,
        name: String,
        kind: String
    ): CategoryEntity? {
        val existing = repo.categoryByName(name, kind)
        if (existing != null) return existing
        val id = repo.saveCategory(
            CategoryEntity(name = name, kind = kind, icon = "more", sortOrder = 99)
        )
        return repo.categoryById(id)
    }

    private fun parseAmount(raw: String): Long? {
        val cleaned = raw.replace("¥", "").replace("￥", "").replace(",", "")
            .replace("元", "").trim()
        if (cleaned.isEmpty()) return null
        val negative = cleaned.startsWith("-")
        val value = cleaned.removePrefix("-").toDoubleOrNull() ?: return null
        if (value <= 0.0) return null
        return Math.round(value * 100)
    }

    private fun parseTime(raw: String): LocalDateTime? {
        if (raw.isBlank()) return null
        for (fmt in timeFormats) {
            try {
                return LocalDateTime.parse(raw.trim(), fmt)
            } catch (_: Throwable) {
                // 试下一个格式
            }
        }
        return null
    }
}
