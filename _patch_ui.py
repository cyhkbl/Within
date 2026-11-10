import io

# 现金渠道
p = r"app\src\main\java\io\github\cyhkbl\within\data\DefaultData.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace('Triple("现金", "CASH", "payments")', 'Triple("现金", "cash", "payments")', 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)

# 底部导航：Expressive 风格（更大图标 + 更高指示器）
p = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {"""
new = """            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0)
            ) {"""
if old in s:
    s = s.replace(old, new, 1); print("navbar OK")
else:
    print("navbar MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)

# 首页标题改用 displaySmall，和资产页统一
p = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold""",
"""                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold""", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("home title OK")

# 统计页标题
p = r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)""",
"""                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)""", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("stats title OK")

# 我的页标题
p = r"app\src\main\java\io\github\cyhkbl\within\ui\more\MoreScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold",
              "style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("more title OK")
