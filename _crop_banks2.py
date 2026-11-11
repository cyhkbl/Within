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

def find_mark(im):
    """返回左侧标志的 (x0,y0,x1,y1)：从左往右扫，遇到足够宽的空白列就断开。"""
    w, h = im.size
    alpha = im.split()[3]
    px = alpha.load()
    # 每列的非透明像素数
    col_ink = []
    for x in range(w):
        count = 0
        for y in range(0, h, 2):
            if px[x, y] > 20:
                count += 1
        col_ink.append(count)

    # 跳过左侧空白
    start = 0
    while start < w and col_ink[start] == 0:
        start += 1
    if start >= w:
        return None

    # 找第一个足够宽的空白带（标志与文字之间）
    gap_needed = max(6, h // 20)
    end = w
    x = start
    while x < w:
        if col_ink[x] == 0:
            run = 0
            while x < w and col_ink[x] == 0:
                run += 1
                x += 1
            if run >= gap_needed:
                end = x - run
                break
        else:
            x += 1

    # 垂直范围
    sub = im.crop((start, 0, end, h))
    bbox = sub.split()[3].getbbox()
    if bbox is None:
        return None
    y0, y1 = bbox[1], bbox[3]
    return (start, y0, end, y1)

for src, name in MAP.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p):
        print("MISS", src); continue
    im = Image.open(p).convert("RGBA")
    mark = find_mark(im)
    if mark is None:
        print("NO MARK", src); continue
    x0, y0, x1, y1 = mark
    # 取正方形，居中
    side = max(x1 - x0, y1 - y0)
    cx, cy = (x0 + x1) // 2, (y0 + y1) // 2
    left = max(0, cx - side // 2)
    top = max(0, cy - side // 2)
    crop = im.crop((left, top, left + side, top + side))
    pad = int(side * 0.08)
    canvas = Image.new("RGBA", (side + pad * 2, side + pad * 2), (0, 0, 0, 0))
    canvas.paste(crop, (pad, pad))
    canvas = canvas.resize((192, 192), Image.LANCZOS)
    canvas.save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} mark=({x0},{y0})-({x1},{y1}) size={side}")
