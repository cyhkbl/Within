import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\home\HomeScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = "                text = stringResource(R.string.home_00),"
new = """                text = if (state.month == YearMonth.now()) {
                    stringResource(R.string.home_00)
                } else {
                    stringResource(R.string.home_month_expense_fmt, state.month.monthValue)
                },"""
if old in s:
    s = s.replace(old, new, 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("OK")
else:
    print("MISS")
