import io
p = r"app\src\main\java\io\github\cyhkbl\within\onboarding\OnboardingScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

# 自启动从可选改为必需
s = s.replace("""            PermissionItem(
                key = "autostart",
                icon = "rocket_launch",
                title = "自启动（可选）",
                description = "一加、OPPO 这类系统会限制后台自启，去系统设置里允许本应用自启动会更稳。",
                check = { AutoRecordPermissions.allGranted(it) },
                open = { AutoRecordPermissions.openAutoStartSettings(it) },
                optional = true
            )""",
"""            PermissionItem(
                key = "autostart",
                icon = "rocket_launch",
                title = "允许自启动",
                description = "一加、OPPO 默认禁止应用自启动。不开的话，重启手机或闲置一段时间后本应用就再也不会被唤起，记账也就断了。",
                check = { AutoRecordPermissions.autoStartLikelyAllowed(it) },
                open = { AutoRecordPermissions.openAutoStartSettings(it) }
            )""")

# 全部就绪时提示去最近任务里锁一下
s = s.replace("""            Text(
                "没授的权限可以随时在「我的 → 自动记账」里补。",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )""",
"""            if (allDone) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        SymbolText(
                            "push_pin",
                            size = 18.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "最后一件事：打开最近任务，把「量入为出」的卡片往下拉（或长按）锁定住。这样系统清理后台时不会把它一起划掉。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Text(
                "没授的权限可以随时在「我的 → 自动记账」里补。",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("Onboarding updated")

# 加一个「自启动大概开着没」的判断
p = r"app\src\main\java\io\github\cyhkbl\within\onboarding\AutoRecordPermissions.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "autoStartLikelyAllowed" not in s:
    s = s.replace("""    /** 四项是否都齐了。 */""",
"""    /**
     * 自启动没有公开的查询接口，没法真的检测。
     * 退一步：能读到我们的电池白名单就认为用户已经在系统里动过手了。
     */
    fun autoStartLikelyAllowed(context: Context): Boolean =
        batteryOptimizationIgnored(context)

    /** 四项是否都齐了。 */""")
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("permission helper added")
