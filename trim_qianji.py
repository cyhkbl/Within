"""
把钱迹导出的 xlsx 处理成「从 0 累加正好等于目标余额」的版本。

做法：
1. 保留全部原始流水（含「平账」，那是真实收支，删掉会让统计失真）
2. 对每个账户算出全量累加值，差额 = 目标余额 - 累加值
3. 在最前面插一笔「期初余额」，金额就是这个差额，账单标记写「不计入收支」

这样导入后从 0 开始逐笔累加，正好落在钱迹的余额上，而这笔期初余额也不进统计。

用法：
    python trim_qianji.py <输入.xlsx> <输出.xlsx> 账户名=余额 ...
"""
import sys, re, copy
import zipfile
from xml.etree import ElementTree as ET
from decimal import Decimal
from collections import OrderedDict

NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
NSP = "{%s}" % NS
# 不注册默认命名空间的话，ElementTree 会把标签写成 ns0:c，Excel 和我们的解析器都认不出来
ET.register_namespace("", NS)

RECONCILE = re.compile(r"平账\s*[（(]\s*([-\d.]+)\s*[~～]\s*([-\d.]+)\s*[）)]")
MARK_EXCLUDE = "不计入收支"


def read_sheet(path):
    z = zipfile.ZipFile(path)
    shared = []
    if "xl/sharedStrings.xml" in z.namelist():
        root = ET.fromstring(z.read("xl/sharedStrings.xml"))
        for si in root.findall(NSP + "si"):
            shared.append("".join(t.text or "" for t in si.iter(NSP + "t")))
    sheet = ET.fromstring(z.read("xl/worksheets/sheet1.xml"))
    return z, shared, sheet


def cell_text(shared, c):
    v = c.find(NSP + "v")
    if v is None or v.text is None:
        isv = c.find(NSP + "is")
        if isv is not None:
            return "".join(t.text or "" for t in isv.iter(NSP + "t"))
        return ""
    return shared[int(v.text)] if c.get("t") == "s" else v.text


def rows_of(sheet, shared):
    out = OrderedDict()
    for row in sheet.iter(NSP + "row"):
        idx = row.get("r")
        cells = {}
        for c in row.findall(NSP + "c"):
            letters = re.match(r"([A-Z]+)", c.get("r")).group(1)
            cells[letters] = cell_text(shared, c)
        out[idx] = cells
    return out


def fmt_money(d: Decimal) -> str:
    # 只保留两位小数，去尾零
    s = format(d.quantize(Decimal("0.01")), "f")
    if s.endswith(".00"):
        s = s[:-3]
    return s


def main():
    if len(sys.argv) < 4:
        print(__doc__)
        return
    src, dst = sys.argv[1], sys.argv[2]
    targets = OrderedDict()
    for arg in sys.argv[3:]:
        name, _, value = arg.partition("=")
        targets[name] = Decimal(value)

    z, shared, sheet = read_sheet(src)
    rows = rows_of(sheet, shared)
    header = rows.pop("1", {})
    print("原始数据行:", len(rows))

    # 1) 平账记录保留——它们是用户主动做的收支调整，删掉会让收支统计失真
    kept = OrderedDict(rows)
    reconciles = sum(1 for c in kept.values() if RECONCILE.search(c.get("K", "")))
    print("保留平账记录:", reconciles)

    # 2) 各账户全量累加
    acc = {a: Decimal(0) for a in targets}
    for cells in kept.values():
        a1, a2 = cells.get("I", ""), cells.get("J", "")
        kind = cells.get("F", "")
        try:
            amt = Decimal(cells.get("G", "0").replace(",", "") or "0")
        except Exception:
            continue
        for a in targets:
            if a not in (a1, a2):
                continue
            if kind == "支出":
                acc[a] -= amt
            elif kind == "收入":
                acc[a] += amt
            elif kind == "转账":
                acc[a] += amt if a == a2 else -amt
            elif kind == "退款":
                acc[a] += amt

    print("\n期初余额 = 目标 - 全量累加：")
    opening = OrderedDict()
    for a, target in targets.items():
        diff = target - acc[a]
        opening[a] = diff
        print(f"  {a:8s} 累加 {acc[a]:10.2f}  目标 {target:10.2f}  期初 {diff:10.2f}")

    # 3) 构造期初余额行（插在最前，时间早于所有记录）
    sheet_data = sheet.find(NSP + "sheetData")
    existing_nums = [int(n) for n in kept.keys()]
    max_num = max(existing_nums) if existing_nums else 1

    new_rows = []
    n = 0
    for a, diff in opening.items():
        if diff == 0:
            continue
        n += 1
        num = max_num + n
        row_el = ET.Element(NSP + "row", {"r": str(num), "spans": "1:19"})
        values = {
            "A": "opening-" + str(n),
            "B": "2000-01-01 00:00:%02d" % n,
            "C": "日常账本",
            "D": "其它",
            "F": "收入" if diff > 0 else "支出",
            "G": fmt_money(abs(diff)),
            "H": "CNY",
            "I": a,
            "K": "期初余额",
            "P": MARK_EXCLUDE,
        }
        for col, val in values.items():
            c = ET.SubElement(row_el, NSP + "c", {"r": col + str(num)})
            if col in ("G",):
                v = ET.SubElement(c, NSP + "v")
                v.text = val
            else:
                c.set("t", "inlineStr")
                is_el = ET.SubElement(c, NSP + "is")
                t = ET.SubElement(is_el, NSP + "t")
                t.text = val
        new_rows.append(row_el)

    print("插入期初余额行:", len(new_rows))

    # 4) 重排所有行号：表头 1，期初在最前，其余按原顺序
    all_rows = []
    for row in sheet_data.findall(NSP + "row"):
        if row.get("r") == "1":
            continue
        if row.get("r") in kept:
            all_rows.append((row.get("r"), row))
    all_rows.sort(key=lambda x: (x[1].find(NSP + "c").get("r") and 0, int(x[0])))

    for row in list(sheet_data.findall(NSP + "row")):
        sheet_data.remove(row)

    header_row = ET.Element(NSP + "row", {"r": "1"})
    for col, val in header.items():
        c = ET.SubElement(header_row, NSP + "c", {"r": col + "1", "t": "inlineStr"})
        is_el = ET.SubElement(c, NSP + "is")
        t = ET.SubElement(is_el, NSP + "t")
        t.text = val
    sheet_data.append(header_row)

    seq = 1
    for row_el in new_rows:
        seq += 1
        row_el.set("r", str(seq))
        for c in row_el.findall(NSP + "c"):
            letters = re.match(r"([A-Z]+)", c.get("r")).group(1)
            c.set("r", letters + str(seq))
        sheet_data.append(row_el)

    for old_num, row in all_rows:
        seq += 1
        row.set("r", str(seq))
        for c in row.findall(NSP + "c"):
            letters = re.match(r"([A-Z]+)", c.get("r")).group(1)
            c.set("r", letters + str(seq))
        sheet_data.append(row)

    print("写出总行数:", seq)

    xml = ET.tostring(sheet, encoding="utf-8", xml_declaration=True)
    zin = zipfile.ZipFile(src)
    zout = zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED)
    for item in zin.infolist():
        data = zin.read(item.filename)
        if item.filename == "xl/worksheets/sheet1.xml":
            data = xml
        zout.writestr(item, data)
    zout.close()
    print("写出:", dst)


if __name__ == "__main__":
    main()
