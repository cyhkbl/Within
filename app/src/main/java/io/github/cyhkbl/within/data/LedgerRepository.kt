package io.github.cyhkbl.within.data

import androidx.room.withTransaction
import io.github.cyhkbl.within.data.AccountChannels
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.BudgetEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.LedgerDatabase
import io.github.cyhkbl.within.data.db.TagEntity
import io.github.cyhkbl.within.data.db.TransactionEntity

/**
 * 数据访问统一入口。写账单时同步维护账户余额，避免 UI 层各写各的。
 */
class LedgerRepository(private val db: LedgerDatabase) {

    fun accounts() = db.accountDao().observeActive()
    fun accountsAll() = db.accountDao().observeAll()
    fun categories() = db.categoryDao().observeActive()
    fun categoriesByKind(kind: String) = db.categoryDao().observeByKind(kind)
    fun allCategories() = db.categoryDao().observeAll()
    fun transactions(from: Long, to: Long) = db.transactionDao().observeRange(from, to)
    fun recentTransactions(limit: Int = 200) = db.transactionDao().observeRecent(limit)
    suspend fun recentTransactionsOnce(limit: Int = 100_000) = db.transactionDao().listRecent(limit)
    suspend fun transactionsOnce(from: Long, to: Long) = db.transactionDao().listRange(from, to)
    fun totalBalance() = db.accountDao().observeTotalBalance()
    fun expenseSum(from: Long, to: Long) = db.transactionDao().observeExpenseSum(from, to)
    fun incomeSum(from: Long, to: Long) = db.transactionDao().observeIncomeSum(from, to)
    fun budgets() = db.budgetDao().observeAll()
    fun books() = db.bookDao().observeAll()
    fun tags() = db.tagDao().observeAll()

    suspend fun transactionById(id: Long) = db.transactionDao().byId(id)
    suspend fun categoryById(id: Long) = db.categoryDao().byId(id)
    suspend fun accountById(id: Long) = db.accountDao().byId(id)
    suspend fun accountByName(name: String) = db.accountDao().byName(name)
    suspend fun categoryByName(name: String, kind: String) = db.categoryDao().byName(name, kind)

    suspend fun insertTransaction(tx: TransactionEntity): Long = db.withTransaction {
        val id = db.transactionDao().insert(tx)
        applyBalanceDelta(tx, 1)
        id
    }

    suspend fun updateTransaction(old: TransactionEntity, new: TransactionEntity) = db.withTransaction {
        applyBalanceDelta(old, -1)
        db.transactionDao().update(new)
        applyBalanceDelta(new, 1)
    }

    suspend fun deleteTransaction(tx: TransactionEntity) = db.withTransaction {
        applyBalanceDelta(tx, -1)
        db.transactionDao().delete(tx)
    }

    /**
     * 老数据的账户类型是 CASH/WALLET/BANK 这类粗分类，这里按账户名一次性迁到
     * 具体渠道（微信、支付宝、工商银行……），只跑一次。
     */
    suspend fun migrateAccountChannels(): Int {
        val accounts = db.accountDao().listAll()
        var changed = 0
        accounts.forEach { acc ->
            val known = AccountChannels.byKey(acc.type)
            val inferred = AccountChannels.inferFromName(acc.name, acc.type)
            // 三种情况要改：
            // 1. 老数据是 CASH/WALLET 这类已废弃的类型
            // 2. 停在通用「银行卡」，但账户名能认出是哪家银行
            // 3. 停在「其它」，但账户名能认出具体渠道
            val upgradeable = known == null ||
                (acc.type == "bank" && inferred != "bank") ||
                (acc.type == "other" && inferred != "other")
            if (upgradeable && inferred != acc.type) {
                db.accountDao().update(acc.copy(type = inferred))
                changed++
            }
        }
        return changed
    }

    /**
     * 把某个账户的余额校准到目标值，差额记一条「平账」。
     *
     * 应用内直接改余额是用户主动的对账动作，所以这笔差额算真实收支，
     * 进账单、进统计。从钱迹导入时若需要对齐余额，走的是另一条不计收支的路径。
     */
    suspend fun adjustAccountBalance(accountId: Long, targetBalance: Long): Boolean {
        val account = accountById(accountId) ?: return false
        val diff = targetBalance - account.balance
        if (diff == 0L) return true
        insertTransaction(
            TransactionEntity(
                kind = if (diff > 0) "INCOME" else "EXPENSE",
                amount = kotlin.math.abs(diff),
                accountId = accountId,
                note = "平账",
                timestamp = System.currentTimeMillis(),
                excludeFromStats = false,
                source = "ADJUST"
            )
        )
        return true
    }

    /**
     * 把账户余额直接设成某个值，不生成任何账单。
     * 导入钱迹数据时用：那边的「平账」记录自带真实余额，按它校准最准。
     */
    suspend fun forceAccountBalance(accountId: Long, value: Long) {
        db.accountDao().setBalance(accountId, value)
    }

    suspend fun findBySourceRef(ref: String) = db.transactionDao().findBySourceRef(ref)

    suspend fun refundedSum(id: Long) = db.transactionDao().refundedSum(id)
    suspend fun refundsOf(id: Long) = db.transactionDao().refundsOf(id)
    suspend fun normalizeRefundFlags() = db.transactionDao().normalizeRefundFlags()

    /** 被退款过的原账单 id，这些在统计里要排除掉。 */
    suspend fun refundedOriginIds(): List<Long> = db.transactionDao().refundedOriginIds()

    suspend fun allCategoriesOnce() = db.categoryDao().listAll()
    suspend fun byBatch(batch: String) = db.transactionDao().byBatch(batch)
    suspend fun allFingerprints() = db.transactionDao().allFingerprints()
    suspend fun allSourceRefs() = db.transactionDao().allSourceRefs()

    /** 清空全部账单并把账户余额归零，用于「撤销旧导入、重新导入」。 */
    suspend fun clearTransactionsAndBalances() = db.withTransaction {
        db.transactionDao().deleteAll()
        db.accountDao().resetBalances()
    }
    suspend fun accountsAllOnce() = db.accountDao().listAll()

    private suspend fun applyBalanceDelta(tx: TransactionEntity, sign: Int) {
        when (tx.kind) {
            "EXPENSE" -> db.accountDao().addBalance(tx.accountId, -sign * tx.amount)
            "INCOME" -> db.accountDao().addBalance(tx.accountId, sign * tx.amount)
            "TRANSFER" -> {
                db.accountDao().addBalance(tx.accountId, -sign * tx.amount)
                tx.toAccountId?.let { db.accountDao().addBalance(it, sign * tx.amount) }
            }
        }
    }

    suspend fun saveAccount(account: AccountEntity): Long =
        if (account.id == 0L) db.accountDao().insert(account) else {
            db.accountDao().update(account); account.id
        }

    suspend fun saveCategory(category: CategoryEntity): Long =
        if (category.id == 0L) db.categoryDao().insert(category) else {
            db.categoryDao().update(category); category.id
        }

    suspend fun saveRule(budget: BudgetEntity): Long =
        if (budget.id == 0L) db.budgetDao().insert(budget) else {
            db.budgetDao().update(budget); budget.id
        }

    suspend fun deleteBudget(id: Long) = db.budgetDao().deleteById(id)

    suspend fun addTag(tag: TagEntity) = db.tagDao().insert(tag)
    suspend fun deleteTag(id: Long) = db.tagDao().deleteById(id)

    suspend fun expenseSumByCategory(categoryId: Long, from: Long, to: Long) =
        db.transactionDao().expenseSumByCategory(categoryId, from, to)

    suspend fun seedIfNeeded() = DefaultData.seedIfEmpty(db)
}
