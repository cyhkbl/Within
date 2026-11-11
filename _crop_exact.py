import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

MAP = {
    "abchina": "ic_bank_abc", "boc": "ic_bank_boc", "cmbchina": "ic_bank_cmb",
    "ccb": "ic_bank_ccb", "icbc": "ic_bank_icbc", "bankcomm": "ic_bank_bocom",
    "psbc": "ic_bank_psbc", "pingan": "ic_bank_pingan", "spd": "ic_bank_spdb",
    "cncb": "ic_bank_citic", "unionpay": "ic_bank_unionpay",
}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

for src, name in MAP.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p): continue
    im = Image.open(p).convert("RGB")
    W, H = im.size
    px = im.load()

    # 整个 logo 的边界
    minx, miny, maxx, maxy = W, H, 0, 0
    for y in range(H):
        for x in range(W):
            if is_ink(*px[x, y]):
                minx = min(minx, x); maxx = max(maxx, x)
                miny = min(miny, y); maxy = max(maxy, y)
    if maxx <= minx or maxy <= miny:
        print("EMPTY", src); continue

    h = maxy - miny + 1
    # 标志是正方形，宽度等于整行高度
    crop = im.crop((minx, miny, min(minx + h, maxx + 1), maxy + 1))

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
    padded.resize((192, 192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} bbox=({minx},{miny})-({maxx},{maxy}) h={h} -> {w2}x{h2}")
