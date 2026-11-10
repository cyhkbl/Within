import io, re

# 修正公共组件里的 dp 写法
p = r"app\src\main\java\io\github\cyhkbl\within\ui\components\MonthPickerDialog.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("size = 22.dp()", "size = 22.dp")
s = s.replace("\nprivate fun Int.dp() = androidx.compose.ui.unit.dp * this\n", "\n")
if "import androidx.compose.ui.unit.dp" not in s:
    s = s.replace("import androidx.compose.ui.text.style.TextAlign",
                  "import androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.unit.dp", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("component fixed")

# HomeScreen 改用公共组件，删掉自己那份
p = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
idx = s.find("@Composable\nprivate fun MonthPickerDialog(")
if idx > 0:
    s = s[:idx].rstrip() + "\n"
    print("home picker removed")
if "import io.github.cyhkbl.within.ui.components.MonthPickerDialog" not in s:
    s = s.replace("import io.github.cyhkbl.within.R",
                  "import io.github.cyhkbl.within.R\nimport io.github.cyhkbl.within.ui.components.MonthPickerDialog", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)

# StatsScreen：月份可切换
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("    val ym = remember { YearMonth.now() }",
              "    var ym by remember { mutableStateOf(YearMonth.now()) }\n    var showPicker by remember { mutableStateOf(false) }\n\n    if (showPicker) {\n        MonthPickerDialog(\n            current = ym,\n            onDismiss = { showPicker = false },\n            onSelect = { picked -> ym = picked; showPicker = false }\n        )\n    }", 1)
for imp in (
    "import androidx.compose.runtime.getValue",
    "import androidx.compose.runtime.mutableStateOf",
    "import androidx.compose.runtime.setValue",
    "import io.github.cyhkbl.within.ui.components.MonthPickerDialog",
):
    if imp not in s:
        s = s.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.remember\n" + imp, 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("stats patched")
