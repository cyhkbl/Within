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

def find_mark(im):
    w, h = im.size
    rgb = im.convert("RGB")
    px = rgb.load()
    col_ink = []
    for x in range(w):
        c = 0
        for y in range(0, h, 2):
            r, g, b = px[x, y]
            if is_ink(r, g, b):
                c += 1
        col_ink.append(c)

    start = 0
    while start < w and col_ink[start] == 0:
        start += 1
    if start >= w:
        return None

    gap_needed = max(6, h // 24)
    end = w
    x = start
    while x < w:
        if col_ink[x] == 0:
            run = 0
            while x < w and col_ink[x] == 0:
                run += 1
                x += 1
            if run >= gap_needed and x < w:
                end = x - run
                break
        else:
            x += 1

    sub = rgb.crop((start, 0, end, h))
    # 找垂直墨水范围
    pix = sub.load()
    ymin, ymax = h, 0
    for y in range(h):
        for xx in range(0, sub.width, 2):
            r, g, b = pix[xx, y]
            if is_ink(r, g, b):
                ymin = min(ymin, y)
                ymax = max(ymax, y)
                break
    if ymin > ymax:
        return None
    return (start, ymin, end, ymax + 1)

for src, name in MAP.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p):
        print("MISS", src); continue
    src_im = Image.open(p).convert("RGBA")
    mark = find_mark(src_im)
    if mark is None:
        print("NO MARK", src); continue
    x0, y0, x1, y1 = mark

    # 白底转透明
    rgb = src_im.convert("RGB")
    out = Image.new("RGBA", src_im.size, (0, 0, 0, 0))
    sp = rgb.load(); op = out.load()
    for y in range(src_im.height):
        for x in range(src_im.width):
            r, g, b = sp[x, y]
            if is_ink(r, g, b):
                op[x, y] = (r, g, b, 255)

    side = max(x1 - x0, y1 - y0)
    cx, cy = (x0 + x1) // 2, (y0 + y1) // 2
    left = max(0, cx - side // 2); top = max(0, cy - side // 2)
    crop = out.crop((left, top, left + side, top + side))
    pad = max(2, int(side * 0.06))
    canvas = Image.new("RGBA", (side + pad * 2, side + pad * 2), (0, 0, 0, 0))
    canvas.paste(crop, (pad, pad))
    canvas.resize((192, 192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} mark=({x0},{y0})-({x1},{y1})")
