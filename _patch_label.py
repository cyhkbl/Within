import io

# 资源：按月份显示的支出标题
for lang, text in (("values", "%1$d月支出"), ("values-en", "Spent in %1$d")):
    p = r"app\src\main\res\%s\strings.xml" % lang
    s = io.open(p, encoding="utf-8", newline="").read()
    if "home_month_expense_fmt" not in s:
        s = s.replace("</resources>", '    <string name="home_month_expense_fmt">%s</string>\n</resources>' % text, 1)
        io.open(p, "w", encoding="utf-8", newline="").write(s)
        print(lang, "OK")

# HomeScreen：往月显示「N月支出」
p = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = '                    Text(stringResource(R.string.home_00), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))'
new = '''                    Text(
                        if (state.month == YearMonth.now()) stringResource(R.string.home_00)
                        else stringResource(R.string.home_month_expense_fmt, state.month.monthValue),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )'''
if old in s:
    s = s.replace(old, new, 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("label OK")
else:
    print("label MISS")
