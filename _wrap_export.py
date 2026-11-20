import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\io\ImportExportActivity.kt"
lines = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n").split("\n")

# 找「导出」块的起止：从注释行到对应的 item 结束
start = next(i for i, l in enumerate(lines) if l.strip() == "// ── 导出 ──")
# 从 start 往后找第一处 item { ... } 的闭合：用大括号计数
brace = 0
end = None
for i in range(start, len(lines)):
    brace += lines[i].count("{") - lines[i].count("}")
    if i > start and brace == 0:
        end = i
        break

if end is None:
    print("MISS end")
else:
    block = lines[start:end + 1]
    new = ["        if (showExport) {"] + ["    " + l for l in block] + ["        }"]
    lines = lines[:start] + new + lines[end + 1:]
    io.open(p, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
    print("export block wrapped, lines", start, "to", end)
