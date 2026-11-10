import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

dialog = '''

@Composable
private fun CalibrateDialog(
    accounts: List<io.github.cyhkbl.within.data.db.AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Map<Long, Long>) -> Unit
) {
    val inputs = androidx.compose.runtime.remember(accounts) {
        androidx.compose.runtime.mutableStateMapOf<Long, String>().apply {
            accounts.forEach { put(it.id, io.github.cyhkbl.within.util.MoneyUtils.formatPlain(it.balance)) }
        }
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("按钱迹的余额校准") },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.foundation.verticalScroll(
                    androidx.compose.foundation.rememberScrollState()
                )
            ) {
                androidx.compose.material3.Text(
                    "填每个账户在钱迹里显示的余额。差额会记成一条「余额调整」，只改余额，不计入收支统计。",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(10.dp))
                accounts.forEach { acc ->
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            acc.name,
                            modifier = androidx.compose.ui.Modifier.weight(1f)
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = inputs[acc.id] ?: "",
                            onValueChange = { inputs[acc.id] = it },
                            singleLine = true,
                            modifier = androidx.compose.ui.Modifier.width(150.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            )
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val targets = accounts.associate { acc ->
                    val raw = inputs[acc.id]?.toDoubleOrNull() ?: 0.0
                    acc.id to Math.round(raw * 100)
                }
                onConfirm(targets)
            }) { androidx.compose.material3.Text("校准") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("取消")
            }
        }
    )
}
'''
s = s.rstrip() + "\n" + dialog
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("dialog appended")
