import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(stringResource(R.string.stats_00), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    DateUtils.formatMonthLabel(ym),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }"""
new = """            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(stringResource(R.string.stats_00), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .clickable { showPicker = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        DateUtils.formatMonthLabel(ym),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.cyhkbl.within.ui.components.placeholder()
                }
            }"""
# 上面那行占位是笔误，改成正常写法
new = new.replace("                    androidx.cyhkbl.within.ui.components.placeholder()\n", "                    SymbolText(\"expand_more\", size = 16.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)\n")
if old in s:
    s = s.replace(old, new, 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("stats title clickable OK")
else:
    print("MISS")
