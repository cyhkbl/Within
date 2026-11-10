import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

old = """            FloatingActionButton(
                onClick = { creating = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 26.dp)
            }"""
new = """            LargeFloatingActionButton(
                onClick = { creating = true },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText(
                    "add",
                    size = 30.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }"""
if old in s:
    s = s.replace(old, new, 1); print("OK fab")
else:
    print("MISS fab")

s = s.replace("import androidx.compose.material3.FloatingActionButton\n",
              "import androidx.compose.material3.LargeFloatingActionButton\n")
io.open(p, "w", encoding="utf-8", newline="").write(s)
