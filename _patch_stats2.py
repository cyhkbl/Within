import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
for imp in ("import androidx.compose.foundation.clickable",
            "import androidx.compose.foundation.shape.RoundedCornerShape",
            "import androidx.compose.ui.draw.clip",
            "import androidx.compose.ui.unit.dp",
            "import io.github.cyhkbl.within.ui.components.SymbolText"):
    if imp not in s:
        anchor = "import androidx.compose.runtime.remember"
        s = s.replace(anchor, anchor + "\n" + imp, 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("imports added")
