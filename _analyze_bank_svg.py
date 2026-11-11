import io, os, re

D = r"samples\payment-webfont-cn\svg"
for f in sorted(os.listdir(D)):
    if not f.endswith(".svg"): continue
    s = io.open(os.path.join(D, f), encoding="utf-8").read()
    paths = re.findall(r"<path\b[^>]*?/>", s, re.S)
    # 找带 transform translate 的、坐标在 0..40 的圆标 path
    circle_like = []
    for p in paths:
        d = re.search(r'\sd="([^"]+)"', p)
        if not d: continue
        dd = d.group(1)
        nums = [float(x) for x in re.findall(r"-?\d+\.?\d*", dd)[:8]]
        if not nums: continue
        if all(-5 < n < 60 for n in nums) and len(dd) > 300:
            circle_like.append((len(dd), dd[:60]))
    print(f"{f:18s} paths={len(paths):3d} circle_candidates={len(circle_like)}")
    for n, head in circle_like[:2]:
        print(f"      len={n} {head}")
