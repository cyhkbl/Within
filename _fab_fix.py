import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\category\CategoryManageActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

old = """        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showDialog = true },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 26.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },"""
new = """        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { editing = null; showDialog = true },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 30.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },"""
if old in s:
    s = s.replace(old, new, 1); print("FAB OK")
else:
    i = s.find("floatingActionButton = {")
    print("MISS, actual:", repr(s[i:i+420]) if i > 0 else "not found")

if "import androidx.compose.material3.LargeFloatingActionButton" not in s:
    s = s.replace("import androidx.compose.material3.FloatingActionButton",
                  "import androidx.compose.material3.FloatingActionButton\nimport androidx.compose.material3.LargeFloatingActionButton", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
