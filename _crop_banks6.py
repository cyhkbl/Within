import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

MAP = {
    "icbc": "ic_bank_icbc", "boc": "ic_bank_boc", "cmbchina": "ic_bank_cmb",
    "ccb": "ic_bank_ccb", "abchina": "ic_bank_abc", "bankcomm": "ic_bank_bocom",
    "psbc": "ic_bank_psbc", "pingan": "ic_bank_pingan", "spd": "ic_bank_spdb",
    "unionpay": "ic_bank_unionpay", "cncb": "ic_bank_citic", "tenpay": "ic_bank_tenpay",
}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

for src, name in MAP.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p):
        print("MISS", src); continue
    im = Image.open(p).convert("RGB")
    W, H = im.size
    px = im.load()

    # 只在左侧 45% 范围内找标志，避免把右侧文字算进来
    limit = int(W * 0.45)
    cols = []
    for x in range(limit):
        ink = any(is_ink(*px[x, y]) for y in range(0, H, 2))
        cols.append(ink)

    # 第一个墨水列 到 最后一列连续墨水的右边界（允许中间小空隙）
    first = next((i for i, c in enumerate(cols) if c), None)
    if first is None:
        print("EMPTY", src); continue
    gap_tol = max(4, H // 30)
    last = first
    gap = 0
    for i in range(first, limit):
        if cols[i]:
            last = i
            gap = 0
        else:
            gap += 1
            if gap > gap_tol:
                break

    # 垂直范围
    ys = [y for y in range(H) if any(is_ink(*px[x, y]) for x in range(first, last + 1, 2))]
    if not ys:
        print("EMPTY2", src); continue
    y0, y1 = min(ys), max(ys) + 1

    crop = im.crop((first, y0, last + 1, y1))
    w, hh = crop.size

    out = Image.new("RGBA", crop.size, (0, 0, 0, 0))
    op = out.load()
    cp = crop.load()
    for y in range(hh):
        for x in range(w):
            r, g, b = cp[x, y]
            if is_ink(r, g, b):
                op[x, y] = (r, g, b, 255)

    s = max(w, hh)
    canvas = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    canvas.paste(out, ((s - w) // 2, (s - hh) // 2))
    pad = max(2, int(s * 0.08))
    padded = Image.new("RGBA", (s + pad * 2, s + pad * 2), (0, 0, 0, 0))
    padded.paste(canvas, (pad, pad))
    padded.resize((192, 192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} mark x={first}..{last} y={y0}..{y1} -> {w}x{hh}")
