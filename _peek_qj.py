import zipfile, re
from xml.etree import ElementTree as ET

NS = {"m": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
z = zipfile.ZipFile(r"samples\qianji-1121.xlsx")

# 共享字符串
ss = []
try:
    root = ET.fromstring(z.read("xl/sharedStrings.xml"))
    for si in root.findall("m:si", NS):
        ss.append("".join(t.text or "" for t in si.iter("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}t")))
except KeyError:
    pass

sheet = ET.fromstring(z.read("xl/worksheets/sheet1.xml"))
rows = []
for row in sheet.iter("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}row"):
    cells = {}
    for c in row.findall("m:c", NS):
        ref = c.get("r")
        col = re.match(r"([A-Z]+)", ref).group(1)
        v = c.find("m:v", NS)
        t = c.get("t")
        if v is None:
            continue
        val = v.text
        if t == "s":
            val = ss[int(val)]
        cells[col] = val
    rows.append(cells)

header = rows[0]
print("表头:", [header.get(c, "") for c in sorted(header, key=lambda x: (len(x), x))][:12])
# 找账户列
acct_cols = [c for c, v in header.items() if "账户" in v or "资产" in v]
print("账户相关列:", [(c, header[c]) for c in acct_cols])

# 统计工商银行
from collections import defaultdict
bal = defaultdict(int)
kinds = defaultdict(int)
for r in rows[1:]:
    name = ""
    for c in acct_cols:
        if c in r:
            name = r[c]
            break
    if "工商" not in name:
        continue
    kind = r.get("C", "") or r.get("D", "")
    amt = 0
    for c in ["E", "F"]:
        if c in r:
            try:
                amt = float(r[c])
                break
            except: pass
    kinds[kind] += 1
    bal[kind] += amt
print("工商银行涉及行数:", sum(kinds.values()), dict(kinds))
print("按类型汇总:", dict(bal))
