import csv, collections
with open(r"samples\converted.csv", encoding="utf-8-sig", newline="") as f:
    rows = list(csv.DictReader(f))

def fp(r):
    return (r["时间"], r["金额"], r["账户1"], r["类型"])

seen = {}
dups = []
for i, r in enumerate(rows, start=2):
    k = fp(r)
    if k in seen:
        dups.append((seen[k], i, k, r["分类"], r["备注"], r["ID"]))
    else:
        seen[k] = i

print("fingerprint duplicates:", len(dups))
for a, b, k, cat, note, rid in dups:
    print(f"  line {a} & {b}: {k}  cat={cat} note={note} id={rid}")

ids = collections.Counter(r["ID"] for r in rows)
print("\nID duplicates:", sum(1 for v in ids.values() if v > 1))
