import io
p = r"app\src\main\java\io\github\cyhkbl\within\autolog\AutoLogSettingsActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

# 1) 紫色工作原理块
start = s.find("""        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,""")
end = s.find("""        item {
            Text(
                stringResource(R.string.autolog_07),""")
if start > 0 and end > start:
    s = s[:start] + s[end:]
    print("removed principle block")
else:
    print("MISS principle", start, end)

# 2) 测试块
start = s.find("""        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SymbolText("science\"""")
end = s.find("""        item { Spacer(Modifier.height(60.dp)) }""")
if start > 0 and end > start:
    s = s[:start] + s[end:]
    print("removed test block")
else:
    print("MISS test", start, end)

# 3) 「支持检测的应用」小字（autolog_26 那条 item，若还在）
marker = """        item {
            Text(
                stringResource(R.string.autolog_26),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

"""
if marker in s:
    s = s.replace(marker, "", 1)
    print("removed detected-apps line")
else:
    print("autolog_26 line already gone")

io.open(p, "w", encoding="utf-8", newline="").write(s)
