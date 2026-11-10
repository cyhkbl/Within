import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
needed = [
    "import androidx.compose.ui.res.stringResource",
    "import kotlinx.coroutines.launch",
]
anchor = "import androidx.compose.ui.Alignment"
add = "\n".join(x for x in needed if x not in s)
if add:
    s = s.replace(anchor, add + "\n" + anchor, 1)
    print("imports added")
io.open(p, "w", encoding="utf-8", newline="").write(s)
