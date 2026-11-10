import csv, collections

with open(r"samples\converted.csv", encoding="utf-8-sig", newline="") as f:
    rows = list(csv.DictReader(f))

def cents(s):
    s = (s or "").replace(",", "").strip()
    if not s:
        return 0
    return round(float(s) * 100)

delta = collections.Counter()
detail = collections.defaultdict(lambda: collections.Counter())

for r in rows:
    kind = r["类型"].strip()
    amt = cents(r["金额"])
    a1 = r["账户1"].strip()
    a2 = r["账户2"].strip()
    if kind == "支出":
        delta[a1] -= amt; detail[a1]["支出"] += amt
    elif kind == "收入":
        delta[a1] += amt; detail[a1]["收入"] += amt
    elif kind == "退款":
        delta[a1] += amt; detail[a1]["退款"] += amt
    elif kind == "转账":
        delta[a1] -= amt; detail[a1]["转出"] += amt
        delta[a2] += amt; detail[a2]["转入"] += amt

qianji = {
    "工商银行": 12673, "中国银行": 17557, "招商银行": 10264,
    "支付宝": 713707, "微信1": 553908, "微信2": 256933,
}
ours = {
    "工商银行": -202835, "中国银行": 17557, "招商银行": 10264,
    "支付宝": 473076, "微信1": 456184, "微信2": 460426,
}

print(f"{'账户':<8}{'流水净额':>12}{'钱迹':>12}{'应用':>12}{'钱迹-流水':>12}")
for name in qianji:
    net = delta[name]
    print(f"{name:<8}{net/100:>12.2f}{qianji[name]/100:>12.2f}{ours[name]/100:>12.2f}{(qianji[name]-net)/100:>12.2f}")

print()
print("流水净额合计: %.2f" % (sum(delta.values())/100))
print("钱迹合计:     15650.42")
print()
for name in qianji:
    d = detail[name]
    print(f"{name}: 支出{d['支出']/100:.2f} 收入{d['收入']/100:.2f} 转出{d['转出']/100:.2f} 转入{d['转入']/100:.2f} 退款{d['退款']/100:.2f}")
