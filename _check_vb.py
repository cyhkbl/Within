import os, re, io
D = r"samples\payment-webfont-cn\svg"
for f in sorted(os.listdir(D)):
    if not f.endswith(".svg"): continue
    s = io.open(os.path.join(D, f), encoding="utf-8").read()
    vb = re.search(r'viewBox="([^"]+)"', s)
    w = re.search(r'\swidth="([^"]+)"', s)
    h = re.search(r'\sheight="([^"]+)"', s)
    print("%-18s viewBox=%-24s w=%-12s h=%s" % (
        f, vb.group(1) if vb else "MISSING",
        w.group(1) if w else "?", h.group(1) if h else "?"))
