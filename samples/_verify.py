import zipfile, re, collections
from xml.etree import ElementTree as ET

NS = "{http://schemas.openxmlformats.org/spreadsheetml/2006/main}"
z = zipfile.ZipFile(r"samples\qianji.xlsx")

shared = []
root = ET.fromstring(z.read("xl/sharedStrings.xml"))
for si in root.findall(NS + "si"):
    shared.append("".join(t.text or "" for t in si.iter(NS + "t")))

def sheet_rows(name):
    root = ET.fromstring(z.read(name))
    for row in root.iter(NS + "row"):
        cells = {}
        for c in row.findall(NS + "c"):
            ref = c.get("r") or ""
            col = 0
            for ch in ref:
                if ch.isalpha():
                    col = col * 26 + (ord(ch.upper()) - 64)
                else:
                    break
            col -= 1
            t = c.get("t")
            if t == "s":
                v = c.find(NS + "v")
                val = shared[int(v.text)] if v is not None else ""
            elif t == "inlineStr":
                isn = c.find(NS + "is")
                val = "".join(x.text or "" for x in isn.iter(NS + "t")) if isn is not None else ""
            else:
                v = c.find(NS + "v")
                val = v.text if v is not None else ""
            cells[col] = val
        n = max(cells) + 1 if cells else 0
        yield [cells.get(i, "") for i in range(n)]

rows = list(sheet_rows("xl/worksheets/sheet1.xml"))
header = [h.strip() for h in rows[0]]
print("cols:", len(header))
idx = {h: i for i, h in enumerate(header)}

def cell(r, name):
    i = idx.get(name, -1)
    return r[i].strip() if 0 <= i < len(r) else ""

data = rows[1:]
print("data rows:", len(data))

kinds = collections.Counter()
bad = []
transfer_pairs = []
refunds = []

for n, r in enumerate(data, start=2):
    if all(not x.strip() for x in r):
        continue
    raw_kind = cell(r, "类型")
    if "退款" in raw_kind: k = "REFUND"
    elif "转账" in raw_kind: k = "TRANSFER"
    elif "收入" in raw_kind: k = "INCOME"
    elif "支出" in raw_kind: k = "EXPENSE"
    else:
        bad.append((n, raw_kind)); continue
    kinds[k] += 1
    amt = cell(r, "金额")
    try:
        v = round(float(amt) * 100)
    except Exception:
        bad.append((n, "amount=" + amt)); continue
    if k == "TRANSFER":
        transfer_pairs.append((cell(r, "账户1"), cell(r, "账户2"), v))
    if k == "REFUND":
        refunds.append((cell(r, "账户1"), v, cell(r, "关联账单"), cell(r, "分类")))

print("kinds:", dict(kinds))
print("bad rows:", bad)
print()
print("transfers:", len(transfer_pairs), "total", sum(t[2] for t in transfer_pairs) / 100)
for t in transfer_pairs[:3]: print("  ", t)
print()
print("refunds:", refunds)
print()
# 转账双账户净影响
net = collections.Counter()
for a1, a2, v in transfer_pairs:
    net[a1] -= v
    net[a2] += v
print("transfer net effect per account:")
for k, v in net.items(): print(f"  {k}: {v/100:+.2f}")
