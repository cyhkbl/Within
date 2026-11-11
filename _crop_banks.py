import os
from PIL import Image

TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"
os.makedirs(OUT, exist_ok=True)

MAP = {
    "icbc": "ic_bank_icbc", "boc": "ic_bank_boc", "cmbchina": "ic_bank_cmb",
    "ccb": "ic_bank_ccb", "abchina": "ic_bank_abc", "bankcomm": "ic_bank_bocom",
    "psbc": "ic_bank_psbc", "pingan": "ic_bank_pingan", "spd": "ic_bank_spdb",
    "unionpay": "ic_bank_unionpay", "cncb": "ic_bank_citic", "tenpay": "ic_bank_tenpay",
}

for src, name in MAP.items():
    p = os.path.join(TMP, src + ".png")
    if not os.path.exists(p):
        print("MISS", src); continue
    im = Image.open(p).convert("RGBA")
    h = im.height
    side = min(h, im.width)
    # 找出左侧非空区域的宽度，确认标志在哪
    crop = im.crop((0, 0, side, side))
    crop = crop.resize((192, 192), Image.LANCZOS)
    crop.save(os.path.join(OUT, name + ".png"))
    print("saved", name, im.size, "->", crop.size)
