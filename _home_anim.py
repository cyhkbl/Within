import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""            state.groups.forEach { group ->
                item(key = "day-${group.dayKey}") { DayHeader(group) }
                items(group.items, key = { "tx-${it.id}" }) { tx ->
                    TransactionRow(
                        tx = tx,
                        category = state.categories[tx.categoryId],
                        account = state.accounts[tx.accountId],
                        onClick = { onTxClick(tx) }
                    )
                }
            }""",
"""            state.groups.forEach { group ->
                item(key = "day-${group.dayKey}") {
                    Box(Modifier.animateItem()) { DayHeader(group) }
                }
                items(group.items, key = { "tx-${it.id}" }) { tx ->
                    // 记账后有新增/删除时，列表项会平滑地让位而不是瞬间跳动
                    Box(Modifier.animateItem()) {
                        TransactionRow(
                            tx = tx,
                            category = state.categories[tx.categoryId],
                            account = state.accounts[tx.accountId],
                            onClick = { onTxClick(tx) }
                        )
                    }
                }
            }""")

for imp in ["import androidx.compose.foundation.layout.Box"]:
    if imp not in s:
        s = s.replace("import androidx.compose.foundation.layout.Arrangement",
                      imp + "\nimport androidx.compose.foundation.layout.Arrangement", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("home animateItem OK")
