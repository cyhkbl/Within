import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """                                Column(modifier = Modifier.weight(1f)) {
                                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        AccountChannels.labelOf(account.type),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }"""
new = """                                Column(modifier = Modifier.weight(1f)) {
                                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                                    // 名字和渠道一致时不重复显示（「支付宝 / 支付宝」）
                                    val channelLabel = AccountChannels.labelOf(account.type)
                                    if (account.name != channelLabel) {
                                        Text(
                                            channelLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }"""
if old in s:
    s = s.replace(old, new, 1); print("OK")
else:
    print("MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
