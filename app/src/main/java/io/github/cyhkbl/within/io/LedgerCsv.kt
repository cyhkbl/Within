package io.github.cyhkbl.within.io

import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.util.DateUtils
import io.github.cyhkbl.within.util.MoneyUtils
import java.time.format.DateTimeFormatter

/**
 * 导出用的 CSV 写入。
 *
 * 列顺序沿用钱迹的九列：时间,类型,金额,分类,账户,备注,标签,币种,手续费。
 * 转账没有单独的「账户2」列，所以转账的账户写 `转出 > 转入`，导入时再拆开，
 * 这样「导出再导入」不会把转入账户弄丢。
 */
object LedgerCsv {

    private const val HEADER = "时间,类型,金额,分类,账户,备注,标签,币种,手续费"

    /** 转账账户列的分隔符，导入端认它。 */
    const val TRANSFER_SEPARATOR = " > "

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
            val accountCell = if (tx.kind == "TRANSFER" && tx.toAccountId != null) {
                val from = accountNames[tx.accountId] ?: ""
                val to = accountNames[tx.toAccountId] ?: ""
                if (from.isNotEmpty() && to.isNotEmpty()) "$from$TRANSFER_SEPARATOR$to" else from
            } else {
                accountNames[tx.accountId] ?: ""
            }
            val time = DateUtils.toLocalDateTime(tx.timestamp)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            sb.append(
                CsvUtils.row(
                    time,
                    typeLabel,
                    MoneyUtils.formatPlain(tx.amount),
                    categoryNames[tx.categoryId] ?: "",
                    accountCell,
                    tx.note,
                    tx.tags,
                    "CNY",
                    MoneyUtils.formatPlain(tx.fee)
                )
            ).append('\n')
        }
        return sb.toString()
    }
}
