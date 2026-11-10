import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """                modifier = androidx.compose.foundation.verticalScroll(
                    androidx.compose.foundation.rememberScrollState()
                )"""
new = """                modifier = androidx.compose.ui.Modifier.verticalScroll(
                    androidx.compose.foundation.rememberScrollState()
                )"""
if old in s:
    s = s.replace(old, new, 1); print("OK")
else:
    print("MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
