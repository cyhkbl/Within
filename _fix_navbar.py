import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""                windowInsets = WindowInsets(0),
                modifier = Modifier.height(82.dp)""",
"""                windowInsets = WindowInsets(0)""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("navbar height removed")
