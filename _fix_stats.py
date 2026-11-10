import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""
private fun Modifier.background(color: Color): Modifier =
    this.then(androidx.compose.foundation.background(color))

private val ExpenseRedUnused = ExpenseRed
""", "")
s = s.replace("import io.github.cyhkbl.within.ui.theme.ExpenseRed\n", "")
if "import androidx.compose.foundation.background" not in s:
    s = s.replace("import androidx.compose.foundation.Canvas",
                  "import androidx.compose.foundation.Canvas\nimport androidx.compose.foundation.background", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("cleaned")
