import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
needed = [
    "import androidx.compose.foundation.rememberScrollState",
    "import androidx.compose.foundation.verticalScroll",
    "import androidx.compose.runtime.mutableStateOf",
    "import androidx.compose.runtime.remember",
    "import androidx.compose.runtime.getValue",
    "import androidx.compose.runtime.setValue",
    "import kotlinx.coroutines.launch",
]
anchor = "import androidx.compose.runtime.Composable"
add = "\n".join(x for x in needed if x not in s)
if add:
    s = s.replace(anchor, anchor + "\n" + add, 1)
    print("imports:", len(add.split("\n")))
io.open(p, "w", encoding="utf-8", newline="").write(s)
