import zipfile, re
from xml.etree import ElementTree as ET
from collections import defaultdict

NS = {"m": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
z = zipfile.ZipFile(r"samples\qianji-1121.xlsx")
ss = []
root = ET.fromstring(z.read("xl/sharedStrings.xml"))
for si in root.findall("m:si", NS):
    ss.append("".join(t.text or "" for t in si.iter("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}t")))

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

pat = re.compile(r"平账\s*[（(]\s*([\-\d.]+)\s*[~～]\s*([\-\d.]+)\s*[）)]")
found = defaultdict(list)
total = 0
for r in rows[1:]:
    note = r.get("K", "")
    m = pat.search(note)
    if not m: continue
    total += 1
    acct = r.get("I", "") or r.get("J", "")
    found[acct].append((r.get("B", ""), m.group(1), m.group(2), r.get("F", "")))

print("平账记录总数:", total)
for a, items in found.items():
    items.sort()
    print(f"\n{a}: {len(items)} 条")
    for it in items[-3:]:
        print("   ", it)
