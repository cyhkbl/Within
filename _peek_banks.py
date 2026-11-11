import io, re
D = r"samples\payment-webfont-cn\svg"
for name in ["boc", "cmbchina", "ccb", "abchina"]:
    s = io.open(D + "\\" + name + ".svg", encoding="utf-8").read()
    vb = re.search(r'viewBox="([^"]+)"', s)
    w = re.search(r'\swidth="([^"]+)"', s)
    h = re.search(r'\sheight="([^"]+)"', s)
    print(f"=== {name}: viewBox={vb.group(1) if vb else None} width={w.group(1) if w else None} height={h.group(1) if h else None}")
    for i, p in enumerate(re.findall(r"<path\b[^>]*?/>", s, re.S)):
        d = re.search(r'\sd="([^"]+)"', p)
        fill = re.search(r'fill:\s*(#[0-9a-fA-F]{3,6})|fill="([^"]+)"', p)
        tr = re.search(r'transform="([^"]+)"', p)
        dd = d.group(1) if d else ""
        print(f"   path{i}: len={len(dd)} fill={fill.group(0) if fill else '-'} transform={tr.group(1)[:40] if tr else '-'}")
        if i < 3:
            print(f"      {dd[:90]}")
