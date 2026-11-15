import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

FIX = {
    "bankcomm": ("ic_bank_bocom", 0.185),
    "pingan":   ("ic_bank_pingan", 0.42),
    "unionpay": ("ic_bank_unionpay", 0.74),
}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

for src, (name, ratio) in FIX.items():
    p = os.path.join(TMP, src + "@big.png")
    im = Image.open(p).convert("RGB")
    W, H = im.size
    px = im.load()
    minx, miny, maxx, maxy = W, H, 0, 0
    for y in range(0, H, 2):
        for x in range(0, W, 2):
            if is_ink(*px[x, y]):
                minx = min(minx, x); maxx = max(maxx, x)
                miny = min(miny, y); maxy = max(maxy, y)
    total_w = maxx - minx + 1
    cut_w = int(total_w * ratio)
    crop = im.crop((minx, miny, minx + cut_w, maxy + 1))
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
    print(f"{name:16s} ratio={ratio} -> {w2}x{h2}")
