import io

# 1) DAO: 取「被退款的原交易 ID」
p = r"app\src\main\java\io\github\cyhkbl\within\data\db\Daos.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "refundedOriginIds" not in s:
    s = s.replace("""    @Query("UPDATE transactions SET excludeFromStats = 1 WHERE refundedFromId IS NOT NULL AND excludeFromStats = 0")""",
"""    @Query("SELECT DISTINCT refundedFromId FROM transactions WHERE refundedFromId IS NOT NULL")
    suspend fun refundedOriginIds(): List<Long>

    @Query("UPDATE transactions SET excludeFromStats = 1 WHERE refundedFromId IS NOT NULL AND excludeFromStats = 0")""")
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("dao OK")

# 2) Repository 暴露
p = r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "refundedOriginIds" not in s:
    s = s.replace("""    suspend fun normalizeRefundFlags() = db.transactionDao().normalizeRefundFlags()""",
"""    suspend fun normalizeRefundFlags() = db.transactionDao().normalizeRefundFlags()

    /** 被退款过的原账单 id，这些在统计里要排除掉。 */
    suspend fun refundedOriginIds(): List<Long> = db.transactionDao().refundedOriginIds()""")
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("repo OK")
