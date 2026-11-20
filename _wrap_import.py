import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\io\ImportExportActivity.kt"
lines = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n").split("\n")

start = next(i for i, l in enumerate(lines) if l.strip() == "// ── 导入 ──")
end = next(i for i, l in enumerate(lines) if l.strip() == "if (showExport) {") - 1
# 去掉导入块末尾的空行
while end > start and lines[end].strip() == "":
    end -= 1

block = lines[start:end + 1]
new = ["        if (showImport) {"] + ["    " + l for l in block] + ["        }"]
lines = lines[:start] + new + lines[end + 1:]
io.open(p, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
print("import block wrapped", start, end)
