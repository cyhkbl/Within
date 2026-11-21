import io

# 1) AccountDao 加 setBalance
p = r"app\src\main\java\io\github\cyhkbl\within\data\db\Daos.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "setBalance" not in s:
    s = s.replace('''    @Query("UPDATE accounts SET balance = 0")''',
'''    @Query("UPDATE accounts SET balance = :value WHERE id = :id")
    suspend fun setBalance(id: Long, value: Long)

    @Query("UPDATE accounts SET balance = 0")''')
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("dao setBalance OK")

# 2) Repository 暴露
p = r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "forceAccountBalance" not in s:
    s = s.replace('''    suspend fun findBySourceRef(ref: String)''',
'''    /**
     * 把账户余额直接设成某个值，不生成任何账单。
     * 导入钱迹数据时用：那边的「平账」记录自带真实余额，按它校准最准。
     */
    suspend fun forceAccountBalance(accountId: Long, value: Long) {
        db.accountDao().setBalance(accountId, value)
    }

    suspend fun findBySourceRef(ref: String)''')
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("repo forceAccountBalance OK")
