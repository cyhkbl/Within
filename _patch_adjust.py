import io
p = r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
anchor = "    suspend fun findBySourceRef(ref: String) = db.transactionDao().findBySourceRef(ref)"
add = """    /**
     * 把某个账户的余额校准到目标值，差额记一条「余额调整」。
     *
     * 钱迹里手动改过余额会留下调整记录，但导出的账单文件不含这些记录，
     * 所以导入之后账户余额会和钱迹对不上。这里让用户按钱迹的数字校准一次，
     * 调整记录不计入收支统计，只影响账户余额。
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
                note = "余额调整",
                timestamp = System.currentTimeMillis(),
                excludeFromStats = true,
                source = "ADJUST"
            )
        )
        return true
    }

"""
if "adjustAccountBalance" not in s:
    s = s.replace(anchor, add + anchor, 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("repository OK")
else:
    print("already present")
