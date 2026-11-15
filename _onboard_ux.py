import io
p = r"app\src\main\java\io\github\cyhkbl\within\onboarding\OnboardingScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

start = s.find("    val states = items.map { it to it.check(context) }")
end = s.find("            Spacer(Modifier.height(40.dp))\n        }\n    }\n}")
assert start > 0 and end > start, (start, end)

new = '''    val checks = items.associateWith { it.check(context) }
    val pendingRequired = items.filter { !it.optional && checks[it] != true }
    val pendingOptional = items.filter { it.optional && checks[it] != true }
    val doneItems = items.filter { checks[it] == true }
    val required = items.filter { !it.optional }
    val requiredDone = required.count { checks[it] == true }
    val allDone = pendingRequired.isEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                if (allDone) "一切就绪" else "让记账自动发生",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (allDone) {
                    "该开的都开了。下次在微信、支付宝或银行 App 里付完款，记账卡片会自己跳出来。"
                } else {
                    "还差 ${pendingRequired.size} 项授权，才能在支付完成的瞬间把记账卡片递给你。"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(modifier = Modifier.height(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(requiredDone.toFloat() / required.size)
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // 还没开的必需项：直接列出来
            pendingRequired.forEach { item ->
                PermissionCard(item = item, granted = false, onClick = { item.open(context) })
                Spacer(Modifier.height(12.dp))
            }

            // 已完成的收成一行，不再占版面
            if (doneItems.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SymbolText(
                            "check_circle",
                            size = 18.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "已完成 ${doneItems.size} 项：" + doneItems.joinToString("、") { it.title },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 可选项只在没开时提示
            if (pendingOptional.isNotEmpty() && allDone) {
                Text(
                    "想让弹窗更稳，可以再开两项",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                pendingOptional.forEach { item ->
                    PermissionCard(item = item, granted = false, onClick = { item.open(context) })
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    OnboardingPrefs.markDone(context)
                    onFinish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    if (allDone) "开始使用" else "稍后再说",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                "没授的权限可以随时在「我的 → 自动记账」里补。",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
'''

s = s[:start] + new + s[end:]
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("OnboardingScreen rewritten")
