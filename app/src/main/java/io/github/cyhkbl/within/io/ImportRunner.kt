package io.github.cyhkbl.within.io

import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.util.DateUtils
import java.util.UUID

data class ImportPreview(
    val newRows: List<ParsedRow>,
    val duplicateCount: Int,
    val issues: List<ParseIssue>,
    val format: ImportFormat,
    val dataLineCount: Int
)

data class ImportSummary(
    val batchId: String,
    val imported: Int,
    val skippedDuplicate: Int,
    val failed: Int,
    val issues: List<ParseIssue>
)

/**
 * 把解析结果落到数据库。
 *
 * 关键语义：
 * - 转账有两个账户，转出和转入都要动余额；
 * - 退款不增加收入，而是冲抵原账单（找不到原账单时冲抵当月支出）；
 * - 重复导入靠「时间 + 金额 + 账户 + 类型」指纹拦截；
 * - 一次导入打同一个批次号，支持整体撤销。
 */
class ImportRunner(private val repo: LedgerRepository) {

    suspend fun preview(outcome: ParseOutcome): ImportPreview {
        val accounts = repo.accountsAllOnce().associateBy { it.name }
        val existing = repo.allFingerprints()
            .map { fingerprint(it.timestamp, it.amount, it.accountId, it.kind) }
            .toHashSet()

        var dup = 0
        val fresh = ArrayList<ParsedRow>()
        for (row in outcome.rows) {
            val accountId = accounts[row.account]?.id
            if (accountId != null) {
                val ts = DateUtils.fromLocalDateTime(row.clock)
                val kind = effectiveKind(row)
                if (fingerprint(ts, row.amount, accountId, kind) in existing) {
                    dup++
                    continue
                }
            }
            fresh.add(row)
        }
        return ImportPreview(fresh, dup, outcome.issues, outcome.format, outcome.dataLineCount)
    }

    suspend fun run(
        outcome: ParseOutcome,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportSummary {
        val batchId = UUID.randomUUID().toString()
        val rows = outcome.rows
        val total = rows.size
        var done = 0

        val existing = repo.allFingerprints()
            .map { fingerprint(it.timestamp, it.amount, it.accountId, it.kind) }
            .toHashSet()
        val accounts = HashMap<String, AccountEntity>()
        repo.accountsAllOnce().forEach { accounts[it.name] = it }
        val categories = HashMap<String, CategoryEntity>()
        repo.allCategoriesOnce().forEach { categories["${it.kind}|${it.name}"] = it }

        var imported = 0
        var skipped = 0
        val issues = ArrayList<ParseIssue>(outcome.issues)
        val externalMap = HashMap<String, Long>()

        // 第一遍：支出 / 收入 / 转账
        val deferred = ArrayList<ParsedRow>()
        for (row in rows) {
            done++
            onProgress(done, total)
            if (row.kind == "REFUND") {
                deferred.add(row)
                continue
            }
            try {
                val account = accountOf(accounts, row.account)
                val toAccount = if (row.kind == "TRANSFER") accountOf(accounts, row.toAccount) else null
                val ts = DateUtils.fromLocalDateTime(row.clock)
                val fp = fingerprint(ts, row.amount, account.id, row.kind)
                if (fp in existing) {
                    skipped++
                    continue
                }
                val categoryId = when (row.kind) {
                    "EXPENSE", "INCOME" -> categoryOf(categories, row.category, row.kind)?.id
                    else -> null
                }
                val id = repo.insertTransaction(
                    TransactionEntity(
                        kind = row.kind,
                        amount = row.amount,
                        categoryId = categoryId,
                        accountId = account.id,
                        toAccountId = toAccount?.id,
                        note = row.note,
                        timestamp = ts,
                        tags = row.tags,
                        fee = row.fee,
                        excludeFromStats = row.excludeFromStats,
                        source = "IMPORT",
                        sourceRef = row.externalId.ifBlank { null },
                        reimbursable = row.reimbursable,
                        importBatchId = batchId
                    )
                )
                existing.add(fp)
                if (row.externalId.isNotBlank()) externalMap[row.externalId] = id
                imported++
            } catch (t: Throwable) {
                issues.add(ParseIssue(row.lineNo, "写入失败：${t.message ?: "未知错误"}", row.note))
            }
        }

        // 第二遍：退款。能找到原账单就关联，找不到就当作冲抵支出的收入
        for (row in deferred) {
            done++
            onProgress(done, total)
            try {
                val account = accountOf(accounts, row.account)
                val ts = DateUtils.fromLocalDateTime(row.clock)
                val originalId = row.linkedExternalId.takeIf { it.isNotBlank() }?.let { externalMap[it] }
                val categoryId = originalId?.let { repo.transactionById(it)?.categoryId }
                    ?: categoryOf(categories, "退款", "INCOME")?.id
                val fp = fingerprint(ts, row.amount, account.id, "REFUND")
                if (fp in existing) {
                    skipped++
                    continue
                }
                repo.insertTransaction(
                    TransactionEntity(
                        kind = "INCOME",
                        amount = row.amount,
                        categoryId = categoryId,
                        accountId = account.id,
                        note = row.note.ifBlank { "退款" },
                        timestamp = ts,
                        excludeFromStats = true,
                        source = "REFUND",
                        sourceRef = row.externalId.ifBlank { null },
                        refundedFromId = originalId,
                        reimbursable = row.reimbursable,
                        importBatchId = batchId
                    )
                )
                existing.add(fp)
                imported++
            } catch (t: Throwable) {
                issues.add(ParseIssue(row.lineNo, "退款写入失败：${t.message ?: "未知错误"}", row.note))
            }
        }

        return ImportSummary(batchId, imported, skipped, issues.size, issues)
    }

    suspend fun undo(batchId: String): Int {
        val rows = repo.byBatch(batchId)
        rows.forEach { repo.deleteTransaction(it) }
        return rows.size
    }

    private fun effectiveKind(row: ParsedRow): String =
        if (row.kind == "REFUND") "REFUND" else row.kind

    private suspend fun accountOf(cache: HashMap<String, AccountEntity>, name: String): AccountEntity {
        cache[name]?.let { return it }
        val existing = repo.accountByName(name)
        if (existing != null) {
            cache[name] = existing
            return existing
        }
        val id = repo.saveAccount(AccountEntity(name = name, type = "WALLET", sortOrder = 90))
        val created = repo.accountById(id) ?: AccountEntity(id = id, name = name)
        cache[name] = created
        return created
    }

    private suspend fun categoryOf(
        cache: HashMap<String, CategoryEntity>,
        name: String,
        kind: String
    ): CategoryEntity? {
        if (name.isBlank()) return null
        val key = "$kind|$name"
        cache[key]?.let { return it }
        val existing = repo.categoryByName(name, kind)
        if (existing != null) {
            cache[key] = existing
            return existing
        }
        val id = repo.saveCategory(CategoryEntity(name = name, kind = kind, icon = "category", sortOrder = 90))
        val created = repo.categoryById(id)
        if (created != null) cache[key] = created
        return created
    }

    private fun fingerprint(ts: Long, amount: Long, accountId: Long, kind: String): String =
        "$ts|$amount|$accountId|$kind"
}
