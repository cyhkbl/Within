import csv, collections, io

path = r"samples\converted.csv"
with open(path, encoding="utf-8-sig", newline="") as f:
    rows = list(csv.DictReader(f))

print("total rows:", len(rows))
print("columns:", list(rows[0].keys()))
print()
print("=== 类型分布 ===")
for k, v in collections.Counter(r["类型"] for r in rows).items():
    print(f"  {k}: {v}")
print()
print("=== 账单标记分布 ===")
for k, v in collections.Counter(r["账单标记"] or "(empty)" for r in rows).items():
    print(f"  {k}: {v}")
print()
print("=== 账户2 分布 ===")
for k, v in collections.Counter(r["账户2"] or "(empty)" for r in rows).items():
    print(f"  {k}: {v}")
print()
print("=== 二级分类 ===")
for k, v in collections.Counter(r["二级分类"] or "(empty)" for r in rows).items():
    print(f"  {k}: {v}")
print()
print("=== 转账样例 ===")
for r in [x for x in rows if x["类型"] == "转账"][:3]:
    print("  ", {k: r[k] for k in ("时间","类型","金额","账户1","账户2","备注")})
print()
print("=== 退款样例（类型或备注含退款）===")
for r in [x for x in rows if "退款" in (x["类型"] + x["备注"] + x["分类"])][:5]:
    print("  ", {k: r[k] for k in ("时间","类型","金额","分类","账户1","备注","关联账单")})
print()
print("=== 分类集合 ===")
for k, v in sorted(collections.Counter(r["分类"] for r in rows).items(), key=lambda x: -x[1]):
    print(f"  {k}: {v}")
print()
print("=== 账户集合 ===")
for k, v in sorted(collections.Counter(r["账户1"] for r in rows).items(), key=lambda x: -x[1]):
    print(f"  {k}: {v}")
