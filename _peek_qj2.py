import zipfile, re
from xml.etree import ElementTree as ET
from collections import defaultdict

NS = {"m": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
z = zipfile.ZipFile(r"samples\qianji-1121.xlsx")
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
        col = re.match(r"([A-Z]+)", c.get("r")).group(1)
        v = c.find("m:v", NS)
        if v is None: continue
        val = v.text
        if c.get("t") == "s": val = ss[int(val)]
        cells[col] = val
    rows.append(cells)

hdr = rows[0]
print("列映射:", {k: v for k, v in sorted(hdr.items(), key=lambda x: (len(x[0]), x[0]))})

# F=类型 G=金额 I=账户1 J=账户2
by_kind = defaultdict(int)
count = defaultdict(int)
for r in rows[1:]:
    a1, a2 = r.get("I", ""), r.get("J", "")
    if "工商" not in a1 and "工商" not in a2:
        continue
    kind = r.get("F", "")
    try: amt = float(r.get("G", "0").replace(",", ""))
    except: amt = 0
    key = f"{kind}|{'账户1' if '工商' in a1 else '账户2'}"
    by_kind[key] += amt
    count[key] += 1

print("\n工商银行流水：")
for k in sorted(by_kind):
    print(f"  {k:24s} 共 {count[k]:3d} 笔  金额合计 {by_kind[k]:.2f}")

# 所有账户出现情况
accts = defaultdict(int)
for r in rows[1:]:
    for c in ["I", "J"]:
        if r.get(c):
            accts[r[c]] += 1
print("\n所有账户:", dict(accts))
