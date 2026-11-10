import io

# 1) 修正 verticalScroll 的包名
p = r"app\src\main\java\io\github\cyhkbl\within\ui\entry\EntryScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("import androidx.compose.foundation.layout.verticalScroll",
              "import androidx.compose.foundation.verticalScroll")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("import fixed")

# 2) 补通用文案
for lang, cancel, confirm in (("values", "取消", "确定"), ("values-en", "Cancel", "OK")):
    p = r"app\src\main\res\%s\strings.xml" % lang
    s = io.open(p, encoding="utf-8", newline="").read()
    if 'name="common_cancel"' not in s:
        add = '    <string name="common_cancel">%s</string>\n    <string name="common_confirm">%s</string>\n</resources>' % (cancel, confirm)
        s = s.replace("</resources>", add, 1)
        io.open(p, "w", encoding="utf-8", newline="").write(s)
        print(lang, "strings added")
