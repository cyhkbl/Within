import io

# 1) 分类管理 + 按钮改成和账单/资产一样的 LargeFloatingActionButton
p = r"app\src\main\java\io\github\cyhkbl\within\ui\category\CategoryManageActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
import re
m = re.search(r"        floatingActionButton = \{\n            FloatingActionButton\((.|\n)*?\n            \}\n        \},", s)
if m:
    s = s[:m.start()] + """        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { editing = null; showDialog = true },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 30.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },""" + s[m.end():]
    print("category FAB replaced")
else:
    print("MISS category FAB")
    print(s[s.find("floatingActionButton"):s.find("floatingActionButton")+400])
if "import androidx.compose.material3.LargeFloatingActionButton" not in s:
    s = s.replace("import androidx.compose.material3.FloatingActionButton",
                  "import androidx.compose.material3.FloatingActionButton\nimport androidx.compose.material3.LargeFloatingActionButton", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)

# 2) 底栏四个 tab 转场统一
p = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""        enterTransition = {
            slideInVertically(animationSpec = tween(260)) { it / 12 } + fadeIn(tween(220))
        },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = {
            slideOutVertically(animationSpec = tween(240)) { it / 12 } + fadeOut(tween(180))
        }""",
"""        // 底部四个 tab 是平级的，来回切换用同一套淡入淡出，不区分前进/返回
        enterTransition = { fadeIn(tween(180)) },
        exitTransition = { fadeOut(tween(140)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition = { fadeOut(tween(140)) }""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("transitions unified")
