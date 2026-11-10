import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\calendar\CalendarScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("            .height(58.dp)", "            .height(70.dp)", 1)
s = s.replace("""                fontSize = 9.sp,
                color = ExpenseRed,
                maxLines = 1""",
"""                fontSize = 9.sp,
                lineHeight = 11.sp,
                color = ExpenseRed,
                maxLines = 1""", 1)
s = s.replace("""                fontSize = 9.sp,
                color = IncomeGreen,
                maxLines = 1""",
"""                fontSize = 9.sp,
                lineHeight = 11.sp,
                color = IncomeGreen,
                maxLines = 1""", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("calendar cell OK")
