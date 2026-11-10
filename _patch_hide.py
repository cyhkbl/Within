import io

def patch(path, pairs):
    s = io.open(path, encoding="utf-8", newline="").read().replace("\r\n", "\n")
    for old, new, label in pairs:
        if old not in s:
            print("MISS:", label); continue
        s = s.replace(old, new, 1); print("OK:", label)
    io.open(path, "w", encoding="utf-8", newline="").write(s)

# 账单页：余额调整记录不进列表、不进统计
patch(r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeViewModel.kt", [
    ("""    ) { ym, txs, cats, accounts, total ->""",
     """    ) { ym, rawTxs, cats, accounts, total ->
        // 余额调整只为让账户余额自洽，不当作真实收支，列表和统计里都不出现
        val txs = rawTxs.filter { it.source != "ADJUST" }""",
     "home filter"),
])

# 统计页
patch(r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt", [
    ("""    val byCategory = expenses.groupBy { it.categoryId ?: 0L }""",
     """    val byCategory = expenses.filter { it.source != "ADJUST" }.groupBy { it.categoryId ?: 0L }""",
     "stats filter"),
])
