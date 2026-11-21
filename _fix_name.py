import io
p = r"app\src\main\java\io\github\cyhkbl\within\io\ImportRunner.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""                RECONCILE_PATTERN.find(row.note)?.let { m ->""",
"""                reconcilePattern.find(row.note)?.let { m ->""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("pattern name fixed")
