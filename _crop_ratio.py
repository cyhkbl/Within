import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

# 标志宽度占整个横向 logo 的比例（按各自 viewBox 量出来的）
RATIO = {
    "abchina": 0.205,   # 230 x 46
    "boc": 0.30,        # 200 x 60
    "bankcomm": 0.22,   # 200 x 40
    "ccb": 0.22,        # 200 x 40
    "cmbchina": 0.26,   # 243 x 60
    "icbc": 0.20,       # 209 x 39.8
    "psbc": 0.175,      # 350 x 57
    "pingan": 0.21,     # 224 x 46
    "spd": 0.34,        # 562 x 187
    "cncb": 0.25,       # 254 x 60
    "unionpay": 0.66,   # 296 x 190
}
MAP = {k: "ic_bank_" + v for k, v in {
    "icbc":"icbc","boc":"boc","cmbchina":"cmb","ccb":"ccb","abchina":"abc",
    "bankcomm":"bocom","psbc":"psbc","pingan":"pingan","spd":"spdb",
    "unionpay":"unionpay","cncb":"citic"}.items()}

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

for src, ratio in RATIO.items():
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
    padded.resize((256, 256), Image.LANCZOS).save(os.path.join(OUT, MAP[src] + ".png"))
    print(f"{MAP[src]:16s} crop {cut_w}x{ch} -> {w2}x{h2}")
