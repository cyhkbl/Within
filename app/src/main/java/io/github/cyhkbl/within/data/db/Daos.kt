package io.github.cyhkbl.within.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY archived ASC, id ASC")
    fun observeAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun byId(id: Long): BookEntity?

    @Insert
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY archived ASC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, id ASC")
    suspend fun listAll(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun byId(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE name = :name LIMIT 1")
    suspend fun byName(name: String): AccountEntity?

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE archived = 0 AND includeInTotal = 1")
    fun observeTotalBalance(): Flow<Long>

    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :id")
    suspend fun addBalance(id: Long, delta: Long)

    @Query("UPDATE accounts SET balance = 0")
    suspend fun resetBalances()

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Insert
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind AND archived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeByKind(kind: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind AND archived = 0 ORDER BY sortOrder ASC, id ASC")
    suspend fun listByKind(kind: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun byId(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name AND kind = :kind LIMIT 1")
    suspend fun byName(name: String, kind: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun byNameAnyKind(name: String): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY kind ASC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY kind ASC, sortOrder ASC, id ASC")
    suspend fun listAll(): List<CategoryEntity>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC, id DESC")
    fun observeRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC LIMIT :limit")
    suspend fun listRecent(limit: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun byId(id: Long): TransactionEntity?

    @Insert
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Delete
    suspend fun delete(tx: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE kind = 'EXPENSE' AND excludeFromStats = 0 AND timestamp BETWEEN :from AND :to")
    fun observeExpenseSum(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE kind = 'INCOME' AND excludeFromStats = 0 AND timestamp BETWEEN :from AND :to")
    fun observeIncomeSum(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE kind = 'EXPENSE' AND excludeFromStats = 0 AND excludeFromBudget = 0 AND categoryId = :categoryId AND timestamp BETWEEN :from AND :to")
    suspend fun expenseSumByCategory(categoryId: Long, from: Long, to: Long): Long

    @Query("SELECT * FROM transactions WHERE sourceRef = :ref LIMIT 1")
    suspend fun findBySourceRef(ref: String): TransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE refundedFromId = :id AND kind = 'INCOME'")
    suspend fun refundedSum(id: Long): Long

    @Query("SELECT * FROM transactions WHERE refundedFromId = :id ORDER BY timestamp ASC")
    suspend fun refundsOf(id: Long): List<TransactionEntity>

    @Query("UPDATE transactions SET excludeFromStats = 1 WHERE refundedFromId IS NOT NULL AND excludeFromStats = 0")
    suspend fun normalizeRefundFlags(): Int

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC, id DESC")
    suspend fun listRange(from: Long, to: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE importBatchId = :batch ORDER BY id ASC")
    suspend fun byBatch(batch: String): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp = :ts AND amount = :amount AND accountId = :accountId AND kind = :kind")
    suspend fun countFingerprint(ts: Long, amount: Long, accountId: Long, kind: String): Int

    @Query("SELECT timestamp, amount, accountId, kind FROM transactions")
    suspend fun allFingerprints(): List<FingerprintRow>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId IS :categoryId AND period = :period LIMIT 1")
    suspend fun find(categoryId: Long?, period: String): BudgetEntity?

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY id ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/** 去重用的指纹行，只取需要的四列。 */
data class FingerprintRow(
    val timestamp: Long,
    val amount: Long,
    val accountId: Long,
    val kind: String
)
