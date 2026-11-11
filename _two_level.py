import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
text = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
lines = text.split("\n")

start = 323
end = 395
assert "private fun ChannelPickerDialog" in lines[start + 2], lines[start:start+4]

new = '''/** 渠道选择：一级按资金 / 信用 / 充值 / 投资 / 债务分组，有子项的（如银行卡）再进一层。 */
@Composable
private fun ChannelPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onPick: (AccountChannel) -> Unit
) {
    var expanded by remember { mutableStateOf<AccountChannel?>(null) }
    val scrollState = rememberScrollState()
    LaunchedEffect(title, expanded) { scrollState.scrollTo(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                expanded?.label ?: title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
            ) {
                val current = expanded
                if (current != null) {
                    ChannelGrid(
                        channels = AccountChannels.childrenOf(current.key),
                        onPick = { onPick(it) }
                    )
                } else {
                    listOf(
                        AccountChannels.FUND to AccountChannels.fundChannels,
                        AccountChannels.CREDIT to AccountChannels.creditChannels,
                        AccountChannels.RECHARGE to AccountChannels.rechargeChannels,
                        AccountChannels.INVEST to AccountChannels.investChannels,
                        AccountChannels.DEBT to AccountChannels.debtChannels
                    ).forEach { (group, channels) ->
                        Text(
                            AccountChannels.groupLabel(group),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))
                        ChannelGrid(
                            channels = channels,
                            onPick = { option ->
                                if (AccountChannels.hasChildren(option.key)) {
                                    expanded = option
                                } else {
                                    onPick(option)
                                }
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = {
                if (expanded != null) expanded = null else onDismiss()
            }) {
                Text(if (expanded != null) "返回" else "取消")
            }
        }
    )
}

/** 4 列图标网格，有子项的右下角带一个小箭头。 */
@Composable
private fun ChannelGrid(
    channels: List<AccountChannel>,
    onPick: (AccountChannel) -> Unit
) {
    Column {
        channels.chunked(4).forEach { rowChannels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowChannels.forEach { option ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPick(option) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            ChannelGlyph(channel = option.key, size = 44.dp)
                            if (AccountChannels.hasChildren(option.key)) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    SymbolText(
                                        "chevron_right",
                                        size = 13.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                repeat(4 - rowChannels.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
'''

out = lines[:start] + new.split("\n") + lines[end + 1:]
text2 = "\n".join(out)
if "import androidx.compose.foundation.background" not in text2:
    text2 = text2.replace("import androidx.compose.foundation.clickable",
                          "import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable", 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(text2)
print("picker replaced")
