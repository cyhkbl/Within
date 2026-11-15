import io

# 1) 打开 buildConfig
p = r"app\build.gradle.kts"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "buildConfig = true" not in s:
    s = s.replace("""    buildFeatures {
        compose = true
    }""",
"""    buildFeatures {
        compose = true
        buildConfig = true
    }""", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("buildConfig on")

# 2) 删掉预算和周期记账两个菜单项
p = r"app\src\main\java\io\github\cyhkbl\within\ui\more\MoreScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
for line in [
    '        MoreItem("savings", R.string.more_04, R.string.more_05),\n',
    '        MoreItem("event_repeat", R.string.more_06, R.string.more_07),\n',
]:
    if line in s:
        s = s.replace(line, "", 1)
        print("removed", line.strip())

# 3) 重写关于对话框
old_start = s.find("    if (showAbout) {")
old_end = s.find("    showComingSoon?.let { titleRes ->")
assert old_start > 0 and old_end > old_start

new_block = '''    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于") },
            text = {
                Column {
                    Text(
                        "量入为出",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "版本 %s（%d）".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(14.dp))
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
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("关闭") }
            }
        )
    }

'''
s = s[:old_start] + new_block + s[old_end:]

# 4) 确保 import 齐了
for imp in ["import androidx.compose.foundation.layout.height",
            "import androidx.compose.ui.text.font.FontWeight",
            "import io.github.cyhkbl.within.BuildConfig"]:
    if imp not in s:
        s = s.replace("import androidx.compose.material3.MaterialTheme",
                      imp + "\nimport androidx.compose.material3.MaterialTheme", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("About rewritten")
