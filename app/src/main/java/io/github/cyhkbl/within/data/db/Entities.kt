package io.github.cyhkbl.within.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 金额一律用 Long 存「分」，避免浮点误差。 */

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currency: String = "CNY",
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "CASH",          // CASH / BANK / CREDIT / WALLET / INVEST
    val balance: Long = 0L,
    val currency: String = "CNY",
    val icon: String = "wallet",
    val color: Int = 0,
    val sortOrder: Int = 0,
    val includeInTotal: Boolean = true,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories", indices = [Index("kind"), Index("parentId")])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,                   // EXPENSE / INCOME / TRANSFER
    val parentId: Long? = null,
    val icon: String = "more",
    val color: Int = 0,
    val sortOrder: Int = 0,
    val archived: Boolean = false
)

@Entity(
    tableName = "transactions",
    indices = [
        Index("timestamp"),
        Index("categoryId"),
        Index("accountId"),
        Index("sourceRef"),
        Index("bookId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long = 1,
    val kind: String,                   // EXPENSE / INCOME / TRANSFER
    val amount: Long,                   // 分
    val categoryId: Long? = null,
    val accountId: Long,
    val toAccountId: Long? = null,
    val note: String = "",
    val timestamp: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: String = "",              // 逗号分隔的标签 id
    val imagePaths: String = "",
    val excludeFromStats: Boolean = false,
    val excludeFromBudget: Boolean = false,
    val source: String = "MANUAL",      // MANUAL / NOTIFICATION / A11Y / IMPORT / PERIODIC
    val sourcePackage: String? = null,
    val sourceRef: String? = null,      // 去重指纹
    val fee: Long = 0L,
    val refundedFromId: Long? = null,
    val reimbursable: Boolean = false,
    val reimbursementStatus: String = "NONE"
)

@Entity(tableName = "budgets", indices = [Index("categoryId")])
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val period: String = "MONTH",       // MONTH / YEAR
    val categoryId: Long? = null,       // null 表示总预算
    val amount: Long,
    val startDate: Long
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int = 0
)
