import io

path = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
raw = io.open(path, encoding="utf-8", newline="").read()
src = raw.replace("\r\n", "\n")   # 归一化处理，写回时保持 LF

def sub(old, new):
    global src
    if old not in src:
        print("MISS:", old[:50].replace("\n", "\\n"))
        return
    src = src.replace(old, new, 1)
    print("OK:", new.strip().split("\n")[0][:60])

sub("    Scaffold(\n        containerColor = MaterialTheme.colorScheme.surface,\n        bottomBar = {",
    "    Scaffold(\n        containerColor = MaterialTheme.colorScheme.surface,\n        contentWindowInsets = WindowInsets(0),\n        bottomBar = {")

for screen in ("AssetsScreen(repo)", "MoreScreen(repo)", "StatsScreen(repo)"):
    sub("            ) { padding ->\n                Box(modifier = Modifier.padding(padding)) { %s }\n            }" % screen,
        "            ) { padding ->\n                Box(\n                    modifier = Modifier\n                        .padding(padding)\n                        .statusBarsPadding()\n                ) { %s }\n            }" % screen)

sub("            ) { padding ->\n                Box(modifier = Modifier.padding(padding)) {\n                    HomeScreen(",
    "            ) { padding ->\n                Box(\n                    modifier = Modifier\n                        .padding(padding)\n                        .statusBarsPadding()\n                ) {\n                    HomeScreen(")

io.open(path, "w", encoding="utf-8", newline="").write(src)
