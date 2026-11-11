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

def to_transparent(im):
    rgb = im.convert("RGB")
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    sp, op = rgb.load(), out.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b = sp[x, y]
            if is_ink(r, g, b):
                op[x, y] = (r, g, b, 255)
    return out

def bbox_of(im):
    return im.split()[3].getbbox()

for src, name in MAP.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p):
        print("MISS", src); continue
    im = to_transparent(Image.open(p))
    h = im.height

    # 先按「左侧正方形」取，再收缩到实际墨水范围
    side = h
    crop = im.crop((0, 0, side, side))
    bb = bbox_of(crop)
    if bb is None:
        print("EMPTY", src); continue
    x0, y0, x1, y1 = bb

    # 如果横向宽度明显超过高度一半，说明框住了文字，收紧到左边正方形
    if (x1 - x0) > h * 0.75:
        # 以垂直中线为基准，取正方形
        cy = (y0 + y1) // 2
        s = max(y1 - y0, int(h * 0.9))
        left = 0
        top = max(0, cy - s // 2)
        crop2 = im.crop((left, top, left + s, top + s))
        bb2 = bbox_of(crop2)
        if bb2:
            crop = crop2
            x0, y0, x1, y1 = bb2

    mark = crop.crop((x0, y0, x1, y1))
    w, hh = mark.size
    s = max(w, hh)
    canvas = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    canvas.paste(mark, ((s - w) // 2, (s - hh) // 2))
    pad = max(2, int(s * 0.08))
    padded = Image.new("RGBA", (s + pad * 2, s + pad * 2), (0, 0, 0, 0))
    padded.paste(canvas, (pad, pad))
    padded.resize((192, 192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} mark {w}x{hh}")
