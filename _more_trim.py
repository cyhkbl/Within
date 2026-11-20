import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\more\MoreScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

# 标题下的小字
s = s.replace("""                Text(stringResource(R.string.more_18), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.more_19),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )""",
"""                Text(stringResource(R.string.more_18), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)""")

# 每条的副标题
s = s.replace("""                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(item.title), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(item.subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }""",
"""                            Text(
                                stringResource(item.title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )""")

# About：删介绍段、技术栈、图标出处
s = s.replace("""                    Spacer(Modifier.height(14.dp))
                    Text(
                        "一个不联网的记账本。数据全部存在本机，没有账号、没有云同步、没有广告。自动记账读的是系统支付通知和支付结果页，内容不会离开这台设备。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("技术栈", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Material 3 Expressive · Jetpack Compose · Room · Kotlin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("源代码", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "github.com/cyhkbl/Within",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Apache License 2.0 · 账户图标来自 Simple Icons 与 payment-webfont-cn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )""",
"""                    Spacer(Modifier.height(16.dp))
                    Text("源代码", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "github.com/cyhkbl/Within",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("MoreScreen done")
