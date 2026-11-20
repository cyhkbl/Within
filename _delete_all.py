import io

# 1) 字符串
p = r"app\src\main\res\values\strings.xml"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "more_delete_all" not in s:
    s = s.replace('    <string name="more_39">完成</string>',
'''    <string name="more_39">完成</string>
    <string name="more_delete_all">删除全部账单</string>
    <string name="more_delete_all_confirm">这会清空所有账单并把账户余额归零。删完之后可以重新从钱迹导入。确定吗？</string>
    <string name="more_delete_all_done">已删除</string>
    <string name="more_delete_all_cancel">取消</string>
    <string name="more_delete_all_action">删除</string>''')
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("strings OK")

# 2) MoreScreen：加条目 + 点击处理 + 确认框
p = r"app\src\main\java\io\github\cyhkbl\within\ui\more\MoreScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace('''    MoreItem("backup", R.string.more_12, R.string.more_13),''',
'''    MoreItem("backup", R.string.more_12, R.string.more_13),
    MoreItem("delete_sweep", R.string.more_delete_all, R.string.more_delete_all),''')

s = s.replace('''    var showComingSoon by remember { mutableStateOf<Int?>(null) }''',
'''    var showComingSoon by remember { mutableStateOf<Int?>(null) }
    var showDeleteAll by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()''')

s = s.replace('''                                        R.string.more_14 -> showAppearance = true''',
'''                                        R.string.more_delete_all -> showDeleteAll = true
                                        R.string.more_14 -> showAppearance = true''')

s = s.replace('''    if (showAppearance) {''',
'''    if (showDeleteAll) {
        AlertDialog(
            onDismissRequest = { showDeleteAll = false },
            title = { Text(stringResource(R.string.more_delete_all)) },
            text = { Text(stringResource(R.string.more_delete_all_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAll = false
                    scope.launch { repo.clearTransactionsAndBalances() }
                }) {
                    Text(
                        stringResource(R.string.more_delete_all_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAll = false }) {
                    Text(stringResource(R.string.more_delete_all_cancel))
                }
            }
        )
    }

    if (showAppearance) {''')

for imp in ["import androidx.compose.runtime.rememberCoroutineScope",
            "import kotlinx.coroutines.launch"]:
    if imp not in s:
        s = s.replace("import androidx.compose.runtime.mutableStateOf",
                      imp + "\nimport androidx.compose.runtime.mutableStateOf", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("MoreScreen OK")
