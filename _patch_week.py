import io

# 1) Repository 暴露一次性区间查询
p = r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "transactionsOnce" not in s:
    s = s.replace("    suspend fun recentTransactionsOnce(limit: Int = 100_000) = db.transactionDao().listRecent(limit)",
                  "    suspend fun recentTransactionsOnce(limit: Int = 100_000) = db.transactionDao().listRecent(limit)\n    suspend fun transactionsOnce(from: Long, to: Long) = db.transactionDao().listRange(from, to)", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("repo OK")

# 2) HomeViewModel: 最近 7 日不受浏览月份影响
p = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeViewModel.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """        val today = LocalDate.now()
        val weekValues = (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val (from, to) = DateUtils.dayRange(day)
            val gross = txs.filter { it.timestamp in from..to && it.kind == "EXPENSE" && !it.excludeFromStats }
                .sumOf { it.amount }
            val refundsInDay = txs.filter { it.timestamp in from..to && it.refundedFromId != null }
                .sumOf { it.amount }
            (gross - refundsInDay).coerceAtLeast(0L).toFloat()
        }"""
new = """        val today = LocalDate.now()
        // 「最近 7 日」永远指今天往前七天，和正在浏览的月份无关，
        // 所以单独取一次数据，不能复用当月账单。
        val weekStart = DateUtils.dayRange(today.minusDays(6)).first
        val weekEnd = DateUtils.dayRange(today).second
        val weekTx = repo.transactionsOnce(weekStart, weekEnd)
        val weekValues = (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val (from, to) = DateUtils.dayRange(day)
            val gross = weekTx.filter { it.timestamp in from..to && it.kind == "EXPENSE" && !it.excludeFromStats }
                .sumOf { it.amount }
            val refundsInDay = weekTx.filter { it.timestamp in from..to && it.refundedFromId != null }
                .sumOf { it.amount }
            (gross - refundsInDay).coerceAtLeast(0L).toFloat()
        }"""
if old in s:
    s = s.replace(old, new, 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("homevm OK")
else:
    print("homevm MISS")

# 3) 导出入口文案：不再写 Excel
for lang, text in (("values", "导出 CSV"), ("values-en", "CSV export")):
    p = r"app\src\main\res\%s\strings.xml" % lang
    s = io.open(p, encoding="utf-8", newline="").read()
    s = s.replace('<string name="more_11">CSV / Excel</string>', '<string name="more_11">%s</string>' % text)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
print("export label OK")
