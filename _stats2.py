import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""    // 余额调整不是真实消费，统计里不参与
    // 退款单独处理，不进饼图和统计列表
    val txs = rawTxs.filter { it.source != "ADJUST" && it.refundedFromId == null }""",
"""    // 余额调整、平账、退款都不算消费，统计里一律不参与
    val refundCategoryIds = categories.filter { it.name == "退款" }.map { it.id }.toSet()
    val txs = rawTxs.filter {
        it.source != "ADJUST" &&
            it.source != "REFUND" &&
            it.refundedFromId == null &&
            it.categoryId !in refundCategoryIds
    }""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("stats filter updated")
