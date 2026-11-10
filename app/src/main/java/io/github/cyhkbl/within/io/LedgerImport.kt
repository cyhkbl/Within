package io.github.cyhkbl.within.io

import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ImportFormat { CSV, XLSX, UNKNOWN }

/** 一行解析成功的账单。 */
data class ParsedRow(
    val lineNo: Int,
    val externalId: String,
    val clock: LocalDateTime,
    val kind: String,              // EXPENSE / INCOME / TRANSFER / REFUND
    val amount: Long,
    val category: String,
    val account: String,
    val toAccount: String,
    val note: String,
    val tags: String,
    val fee: Long,
    val excludeFromStats: Boolean,
    val linkedExternalId: String,
    val reimbursable: Boolean = false
)

data class ParseIssue(val lineNo: Int, val reason: String, val raw: String)

data class ParseOutcome(
    val format: ImportFormat,
    val rows: List<ParsedRow>,
    val issues: List<ParseIssue>,
    val dataLineCount: Int
)

/**
 * 钱迹账单解析。CSV 与 XLSX 都吃，按表头名字定位列，不依赖列顺序。
 *
 * 钱迹导出的列：ID, 时间, 账本, 分类, 二级分类, 类型, 金额, 币种, 账户1, 账户2,
 * 备注, 已报销, 手续费, 优惠券, 记账者, 账单标记, 标签, 账单图片, 关联账单
 */
object LedgerImport {

    private val timeFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    /** 钱迹与内置分类的叫法差异，导入时归一，避免同一个分类出现两份。 */
    private val categoryAliases = mapOf(
        "汽车/加油" to "汽车加油",
        "汽车加油" to "汽车加油",
        "交通出行" to "交通",
        "收红包" to "红包",
        "转账红包" to "发红包",
        "股票基金" to "理财",
        "水电煤" to "水电煤"
    )

    /** 钱迹的二级分类拼成「父/子」，这样层级不会丢。 */
    fun buildCategoryName(parent: String, child: String): String {
        val p = parent.trim()
        val ch = child.trim()
        return if (p.isNotEmpty() && ch.isNotEmpty()) {
            normalizeCategory("$p/$ch")
        } else {
            normalizeCategory(p.ifEmpty { ch })
        }
    }

    fun normalizeCategory(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return trimmed
        return categoryAliases[trimmed] ?: trimmed
    }

    fun sniff(fileName: String, bytes: ByteArray): ImportFormat {
        val lower = fileName.lowercase(Locale.getDefault())
        val zipMagic = bytes.size > 3 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
        return when {
            lower.endsWith(".xlsx") || (zipMagic && !lower.endsWith(".csv")) -> ImportFormat.XLSX
            lower.endsWith(".csv") || lower.endsWith(".txt") -> ImportFormat.CSV
            zipMagic -> ImportFormat.XLSX
            bytes.isNotEmpty() -> ImportFormat.CSV
            else -> ImportFormat.UNKNOWN
        }
    }

    fun parse(bytes: ByteArray, fileName: String): ParseOutcome {
        val format = sniff(fileName, bytes)
        val table: List<List<String>> = when (format) {
            ImportFormat.XLSX -> runCatching {
                XlsxReader.read(ByteArrayInputStream(bytes))
            }.getOrElse { return ParseOutcome(format, emptyList(), listOf(ParseIssue(0, "无法读取 xlsx：${it.message ?: "文件损坏"}", "")), 0) }

            else -> CsvUtils.parse(bytes.toString(Charsets.UTF_8))
        }
        return build(format, table)
    }

    private fun build(format: ImportFormat, table: List<List<String>>): ParseOutcome {
        if (table.isEmpty()) {
            return ParseOutcome(format, emptyList(), listOf(ParseIssue(0, "文件是空的", "")), 0)
        }

        val headers = table.first().map { it.trim() }
        fun col(vararg names: String): Int = headers.indexOfFirst { h ->
            names.any { n -> h.equals(n, ignoreCase = true) || h.contains(n, ignoreCase = true) }
        }

        val idCol = col("ID", "id")
        val timeCol = col("时间", "日期", "time", "date")
        val kindCol = col("类型", "type")
        val amountCol = col("金额", "amount")
        val categoryCol = col("分类", "category")
        val accountCol = col("账户1", "资产", "账户", "account")
        val toAccountCol = col("账户2", "转入账户", "to account")
        val noteCol = col("备注", "note", "remark")
        val tagCol = col("标签", "tag")
        val feeCol = col("手续费", "fee")
        val markCol = col("账单标记", "标记")
        val subCategoryCol = col("二级分类", "子分类")
        val reimbursedCol = col("已报销")
        val linkCol = col("关联账单", "关联", "link")

        if (amountCol < 0) {
            return ParseOutcome(
                format, emptyList(),
                listOf(ParseIssue(1, "表头里找不到「金额」列，这个文件可能不是账单导出格式", headers.joinToString(","))),
                0
            )
        }

        val rows = ArrayList<ParsedRow>()
        val issues = ArrayList<ParseIssue>()
        val dataLines = table.size - 1

        for (i in 1 until table.size) {
            val cells = table[i]
            val lineNo = i + 1
            if (cells.all { it.isBlank() }) continue

            fun cell(index: Int): String = if (index in cells.indices) cells[index].trim() else ""

            val rawAmount = cell(amountCol)
            val amount = parseAmount(rawAmount)
            if (amount == null || amount <= 0L) {
                issues.add(ParseIssue(lineNo, "金额无法识别：\"$rawAmount\"", cells.joinToString("|")))
                continue
            }

            val rawKind = cell(kindCol)
            val kind = when {
                rawKind.contains("退款") -> "REFUND"
                rawKind.contains("转账") || rawKind.equals("transfer", true) -> "TRANSFER"
                rawKind.contains("收入") || rawKind.equals("income", true) -> "INCOME"
                rawKind.contains("支出") || rawKind.equals("expense", true) -> "EXPENSE"
                rawKind.isBlank() -> "EXPENSE"
                else -> {
                    issues.add(ParseIssue(lineNo, "未知的类型：\"$rawKind\"", cells.joinToString("|")))
                    continue
                }
            }

            val rawTime = cell(timeCol)
            val clock = parseTime(rawTime)
            if (clock == null) {
                issues.add(ParseIssue(lineNo, "时间无法识别：\"$rawTime\"", cells.joinToString("|")))
                continue
            }

            val rawAccount = cell(accountCol)
            // 转账可能写在同一个单元格里（导出时用 " > " 连接），也可能分两列
            val account: String
            val splitToAccount: String
            if (rawAccount.contains(" > ")) {
                val parts = rawAccount.split(" > ", limit = 2)
                account = parts[0].trim()
                splitToAccount = parts[1].trim()
            } else if (rawAccount.contains("->")) {
                val parts = rawAccount.split("->", limit = 2)
                account = parts[0].trim()
                splitToAccount = parts[1].trim()
            } else {
                account = rawAccount
                splitToAccount = ""
            }
            if (account.isBlank()) {
                issues.add(ParseIssue(lineNo, "缺少账户", cells.joinToString("|")))
                continue
            }

            val toAccount = cell(toAccountCol).ifBlank { splitToAccount }
            if (kind == "TRANSFER" && toAccount.isBlank()) {
                issues.add(ParseIssue(lineNo, "转账缺少转入账户", cells.joinToString("|")))
                continue
            }

            val mark = cell(markCol)
            val excluded = mark.contains("不计入") || mark.contains("exclude", true)

            rows.add(
                ParsedRow(
                    lineNo = lineNo,
                    externalId = cell(idCol),
                    clock = clock,
                    kind = kind,
                    amount = amount,
                    category = buildCategoryName(cell(categoryCol), cell(subCategoryCol)),
                    account = account,
                    toAccount = toAccount,
                    note = cell(noteCol),
                    tags = cell(tagCol),
                    fee = parseAmount(cell(feeCol)) ?: 0L,
                    excludeFromStats = excluded,
                    linkedExternalId = cell(linkCol),
                    reimbursable = cell(reimbursedCol).isNotBlank()
                )
            )
        }

        return ParseOutcome(format, rows, issues, dataLines)
    }

    private fun parseAmount(raw: String): Long? {
        if (raw.isBlank()) return null
        val cleaned = raw.replace("¥", "").replace("￥", "")
            .replace(",", "").replace("元", "").trim()
        if (cleaned.isEmpty()) return null
        val negative = cleaned.startsWith("-")
        val value = cleaned.removePrefix("-").toDoubleOrNull() ?: return null
        if (value.isNaN() || value.isInfinite() || value < 0.0) return null
        val cents = Math.round(value * 100)
        return if (negative) -cents else cents
    }

    private fun parseTime(raw: String): LocalDateTime? {
        if (raw.isBlank()) return null
        val text = raw.trim()
        for (fmt in timeFormats) {
            try {
                return LocalDateTime.parse(text, fmt)
            } catch (_: Throwable) {
                // 试下一个
            }
        }
        // Excel 序列日期（1900 基准）兜底
        val serial = text.toDoubleOrNull() ?: return null
        if (serial < 1.0 || serial > 100000.0) return null
        val epoch = java.time.LocalDate.of(1899, 12, 30)
        val days = serial.toLong()
        val fraction = serial - days
        val seconds = Math.round(fraction * 86400)
        return epoch.plusDays(days).atStartOfDay().plusSeconds(seconds)
    }
}
