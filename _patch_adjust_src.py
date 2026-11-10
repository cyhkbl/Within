import io

# 资源
for lang, text in (("values", "余额调整"), ("values-en", "Balance adjustment")):
    p = r"app\src\main\res\%s\strings.xml" % lang
    s = io.open(p, encoding="utf-8", newline="").read()
    if "source_adjust" not in s:
        s = s.replace("</resources>", '    <string name="source_adjust">%s</string>\n</resources>' % text, 1)
        io.open(p, "w", encoding="utf-8", newline="").write(s)

# 详情页显示
p = r"app\src\main\java\io\github\cyhkbl\within\ui\detail\TransactionDetailScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = '''                            "PERIODIC" -> stringResource(R.string.detail_16)'''
new = '''                            "ADJUST" -> stringResource(R.string.source_adjust)
                            "PERIODIC" -> stringResource(R.string.detail_16)'''
if old in s:
    s = s.replace(old, new, 1); print("OK")
else:
    print("MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
