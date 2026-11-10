import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

def sub(old, new, label):
    global s
    if old not in s:
        print("MISS:", label); return
    s = s.replace(old, new, 1); print("OK:", label)

sub("""    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showCalibrate by remember { mutableStateOf(false) }""",
"""    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showCalibrate by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            kotlinx.coroutines.delay(2600)
            toast = ""
        }
    }""", "toast state")

sub("""                scope.launch {
                    targets.forEach { (id, balance) -> repo.adjustAccountBalance(id, balance) }
                }""",
"""                scope.launch {
                    targets.forEach { (id, balance) -> repo.adjustAccountBalance(id, balance) }
                    toast = "余额已按钱迹校准"
                }""", "toast set")

# 在列表顶部插一条提示
sub("""        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,""",
"""        if (toast.isNotEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Text(
                        toast,
                        modifier = androidx.compose.ui.Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,""", "toast item")

io.open(p, "w", encoding="utf-8", newline="").write(s)
