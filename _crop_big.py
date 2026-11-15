import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

MAP = {
    "icbc": "ic_bank_icbc", "boc": "ic_bank_boc", "cmbchina": "ic_bank_cmb",
    "ccb": "ic_bank_ccb", "abchina": "ic_bank_abc", "bankcomm": "ic_bank_bocom",
    "psbc": "ic_bank_psbc", "pingan": "ic_bank_pingan", "spd": "ic_bank_spdb",
    "unionpay": "ic_bank_unionpay", "cncb": "ic_bank_citic",
}
# 标志和文字紧邻的，限制裁切宽度（相对整行高度）
MANUAL = {"pingan": 0.24, "unionpay": 0.75}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

for src, name in MAP.items():
    p = os.path.join(TMP, src + "@big.png")
    if not os.path.exists(p):
        print("MISS", src); continue
    im = Image.open(p).convert("RGB")
    W, H = im.size
    px = im.load()

    minx, miny, maxx, maxy = W, H, 0, 0
    for y in range(0, H, 2):
        for x in range(0, W, 2):
            if is_ink(*px[x, y]):
                minx = min(minx, x); maxx = max(maxx, x)
                miny = min(miny, y); maxy = max(maxy, y)
    if maxx <= minx:
        print("EMPTY", src); continue

    h = maxy - miny + 1
    right = minx + h
    if src in MANUAL:
        right = minx + int(h * MANUAL[src])

    crop = im.crop((minx, miny, min(right, maxx + 1), maxy + 1))
    cw, ch = crop.size
    out = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
    op = out.load(); cp = crop.load()
    for y in range(ch):
        for x in range(cw):
            r, g, b = cp[x, y]
            if is_ink(r, g, b):
                op[x, y] = (r, g, b, 255)

    bb = out.split()[3].getbbox()
    if bb: out = out.crop(bb)
    w2, h2 = out.size
    s = max(w2, h2)
    canvas = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    canvas.paste(out, ((s - w2) // 2, (s - h2) // 2))
    pad = max(2, int(s * 0.08))
    padded = Image.new("RGBA", (s + pad * 2, s + pad * 2), (0, 0, 0, 0))
    padded.paste(canvas, (pad, pad))
    padded.resize((256, 256), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} -> {w2}x{h2}")
