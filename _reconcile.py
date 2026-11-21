import io
p = r"app\src\main\java\io\github\cyhkbl\within\io\ImportRunner.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

# 正则 + 校准逻辑
if "RECONCILE_PATTERN" not in s:
    s = s.replace("""object ImportRunner {""",
"""object ImportRunner {

    /**
     * 钱迹的「平账」记录长这样：平账(126.73 ~ 132.4)，括号里是调整前后的账户余额。
     * 这个新值就是那一刻的真实余额，比拿流水一分分累加可靠得多。
     */
    private val RECONCILE_PATTERN =
        Regex(""" + '"""' + r"平账\s*[（(]\s*([-\d.]+)\s*[~～]\s*([-\d.]+)\s*[）)]" + '"""' + """)
""")

s = s.replace("""                if (row.externalId.isNotBlank()) externalMap[row.externalId] = id
                imported++""",
"""                if (row.externalId.isNotBlank()) externalMap[row.externalId] = id

                // 平账记录直接把余额校准到它记录的新值，
                // 否则从 0 开始累加流水会和钱迹对不上（银行账户尤其容易算成负数）
                RECONCILE_PATTERN.find(row.note)?.let { m ->
                    m.groupValues.getOrNull(2)?.toDoubleOrNull()?.let { target ->
                        repo.forceAccountBalance(account.id, Math.round(target * 100))
                    }
                }
                imported++""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("ImportRunner OK")
