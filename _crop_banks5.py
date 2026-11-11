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
    h = im.height
    crop = im.crop((0, 0, h, h))
    px = crop.load()
    out = Image.new("RGBA", crop.size, (0, 0, 0, 0))
    op = out.load()
    for y in range(crop.height):
        for x in range(crop.width):
            r, g, b = px[x, y]
            if is_ink(r, g, b):
                op[x, y] = (r, g, b, 255)

    bb = out.split()[3].getbbox()
    if bb:
        out = out.crop(bb)

    w, hh = out.size
    s = max(w, hh)
    canvas = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    canvas.paste(out, ((s - w) // 2, (s - hh) // 2))
    pad = max(2, int(s * 0.08))
    padded = Image.new("RGBA", (s + pad * 2, s + pad * 2), (0, 0, 0, 0))
    padded.paste(canvas, (pad, pad))
    padded.resize((192, 192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(f"{name:16s} {w}x{hh}")
