import io, re
p = r"app\src\main\java\io\github\cyhkbl\within\ui\more\MoreScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
lines = s.split("\n")
out = []
removed = []
for line in lines:
    if 'R.string.more_04' in line or 'R.string.more_06' in line:
        removed.append(line.strip())
        continue
    out.append(line)
s = "\n".join(out)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("removed:", removed)
print("remaining MoreItems:")
for l in s.split("\n"):
    if "MoreItem(" in l and "data class" not in l:
        print("   ", l.strip())
