import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

MANUAL = {
    "icbc": 0.28, "bankcomm": 0.28, "psbc": 0.40, "tenpay": 0.26,
    "pingan": 0.24, "unionpay": 0.72,
}
NAME = {
    "icbc": "ic_bank_icbc", "bankcomm": "ic_bank_bocom", "psbc": "ic_bank_psbc",
    "tenpay": "ic_bank_tenpay", "pingan": "ic_bank_pingan", "unionpay": "ic_bank_unionpay",
}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

def save_mark(crop, name):
    cp = crop.load(); w, h = crop.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0)); op = out.load()
    for y in range(h):
        for x in range(w):
            r, g, b = cp[x, y]
            if is_ink(r, g, b):
                op[x, y] = (r, g, b, 255)
    bb = out.split()[3].getbbox()
    if bb:
        out = out.crop(bb)
    w, h = out.size
    s = max(w, h)
    canvas = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    canvas.paste(out, ((s - w) // 2, (s - h) // 2))
    pad = max(2, int(s * 0.08))
    padded = Image.new("RGBA", (s + pad * 2, s + pad * 2), (0, 0, 0, 0))
    padded.paste(canvas, (pad, pad))
    padded.resize((192, 192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    return w, h

for src, ratio in MANUAL.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p): continue
    im = Image.open(p).convert("RGB")
    H = im.height
    w, h = save_mark(im.crop((0, 0, int(H * ratio), H)), NAME[src])
    print(f"{NAME[src]:16s} ratio={ratio} -> {w}x{h}")
