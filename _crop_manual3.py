import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

# 三个标志与文字贴得太近的，手工给像素框（在原渲染图坐标系里）
MANUAL = {
    "pingan":  ("ic_bank_pingan", (0, 0, 190, 46)),
    "tenpay":  ("ic_bank_tenpay", (0, 0, 105, 54)),
    "unionpay":("ic_bank_unionpay", (0, 0, 300, 190)),
}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

for src, (name, box) in MANUAL.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p): continue
    im = Image.open(p).convert("RGB")
    crop = im.crop(box)
    w, h = crop.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    op = out.load(); cp = crop.load()
    for y in range(h):
        for x in range(w):
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
    print(f"{name:18s} box={box} -> {w2}x{h2}")
