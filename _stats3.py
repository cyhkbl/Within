import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""    // 余额调整、平账、退款都不算消费，统计里一律不参与
    val refundCategoryIds = categories.filter { it.name == "退款" }.map { it.id }.toSet()
    val txs = rawTxs.filter {
        it.source != "ADJUST" &&
            it.source != "REFUND" &&
            it.refundedFromId == null &&
            it.categoryId !in refundCategoryIds
    }""",
"""    // 余额调整、平账、退款都不算消费，统计里一律不参与
    val refundCategoryIds = categories.filter { it.name == "退款" }.map { it.id }.toSet()
    var refundedOriginIds by remember { mutableStateOf(emptySet<Long>()) }
    LaunchedEffect(Unit) { refundedOriginIds = repo.refundedOriginIds().toSet() }
    val txs = rawTxs.filter {
        it.source != "ADJUST" &&
            it.source != "REFUND" &&
            it.refundedFromId == null &&
            it.categoryId !in refundCategoryIds &&
            it.id !in refundedOriginIds
    }""")

for imp in ["import androidx.compose.runtime.LaunchedEffect",
            "import androidx.compose.runtime.mutableStateOf",
            "import androidx.compose.runtime.remember",
            "import androidx.compose.runtime.setValue"]:
    if imp not in s:
        s = s.replace("import androidx.compose.runtime.Composable",
                      imp + "\nimport androidx.compose.runtime.Composable", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("stats updated")
