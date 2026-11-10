import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

def sub(old, new, label):
    global s
    if old not in s:
        print("MISS:", label); return
    s = s.replace(old, new, 1); print("OK:", label)

# 状态
sub("""    val accounts by repo.accounts().collectAsState(initial = emptyList())
    val total by repo.totalBalance().collectAsState(initial = 0L)""",
"""    val accounts by repo.accounts().collectAsState(initial = emptyList())
    val total by repo.totalBalance().collectAsState(initial = 0L)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showCalibrate by remember { mutableStateOf(false) }

    if (showCalibrate) {
        CalibrateDialog(
            accounts = accounts,
            onDismiss = { showCalibrate = false },
            onConfirm = { targets ->
                showCalibrate = false
                scope.launch {
                    targets.forEach { (id, balance) -> repo.adjustAccountBalance(id, balance) }
                }
            }
        )
    }""", "state")

# 顶部按钮旁边加一个「校准」
sub("""                    ) {
                        SymbolText("tune", size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.assets_01))
                    }
                }""",
"""                    ) {
                        SymbolText("tune", size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.assets_01))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    OutlinedButton(
                        onClick = { showCalibrate = true },
                        border = null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                    ) {
                        SymbolText("balance", size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("校准")
                    }
                }""", "calibrate button")

io.open(p, "w", encoding="utf-8", newline="").write(s)
