import io
path = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeScreen.kt"
raw = io.open(path, encoding="utf-8", newline="").read()
src = raw.replace("\r\n", "\n")

def sub(old, new, label=""):
    global src
    if old not in src:
        print("MISS:", label or old[:50].replace("\n", "\\n"))
        return
    src = src.replace(old, new, 1)
    print("OK:", label or new.strip()[:50])

# 外层 MainShell 已经给了 statusBarsPadding，这里清零避免双重留白
sub("    Scaffold(\n        containerColor = MaterialTheme.colorScheme.surface,\n        topBar = {",
    "    Scaffold(\n        containerColor = MaterialTheme.colorScheme.surface,\n        contentWindowInsets = WindowInsets(0),\n        topBar = {", "scaffold insets")
sub("            TopAppBar(\n                title = {",
    "            TopAppBar(\n                windowInsets = WindowInsets(0),\n                title = {", "topappbar insets")

for imp in ("import androidx.compose.foundation.layout.WindowInsets",):
    if imp not in src:
        src = src.replace("import androidx.compose.foundation.layout.padding", imp + "\nimport androidx.compose.foundation.layout.padding", 1)

io.open(path, "w", encoding="utf-8", newline="").write(src)
