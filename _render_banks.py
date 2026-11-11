import os, subprocess, re, io
from PIL import Image

EDGE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
SRC = r"samples\payment-webfont-cn\svg"
TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"
os.makedirs(TMP, exist_ok=True)
os.makedirs(OUT, exist_ok=True)

BANKS = {
    "icbc": "ic_bank_icbc",
    "boc": "ic_bank_boc",
    "cmbchina": "ic_bank_cmb",
    "ccb": "ic_bank_ccb",
    "abchina": "ic_bank_abc",
    "bankcomm": "ic_bank_bocom",
    "psbc": "ic_bank_psbc",
    "pingan": "ic_bank_pingan",
    "spd": "ic_bank_spdb",
    "unionpay": "ic_bank_unionpay",
    "cncb": "ic_bank_citic",
    "tenpay": "ic_bank_tenpay",
}

for src, name in BANKS.items():
    svg_path = os.path.join(SRC, src + ".svg")
    if not os.path.exists(svg_path):
        print("MISS", src); continue
    svg = io.open(svg_path, encoding="utf-8").read()
    vb = re.search(r'viewBox="([^"]+)"', svg)
    if vb:
        parts = [float(x) for x in re.split(r"[ ,]+", vb.group(1).strip())]
        ratio = parts[2] / parts[3] if parts[3] else 3.0
    else:
        w = re.search(r'\swidth="([\d.]+)', svg)
        h = re.search(r'\sheight="([\d.]+)', svg)
        ratio = (float(w.group(1)) / float(h.group(1))) if (w and h) else 3.0

    height = 240
    width = int(height * ratio)
    shot = os.path.join(TMP, src + ".png")
    url = "file:///" + svg_path.replace("\\", "/")
    subprocess.run([EDGE, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                    "--screenshot=" + shot, "--window-size=%d,%d" % (width, height), url],
                   capture_output=True, timeout=60)
    if not os.path.exists(shot):
        print("RENDER FAIL", src); continue

    im = Image.open(shot).convert("RGBA")
    # 左侧正方形区域就是圆形标志
    side = min(height, im.width)
    logo = im.crop((0, 0, side, side))
    logo = logo.resize((192, 192), Image.LANCZOS)
    target = os.path.join(OUT, name + ".png")
    logo.save(target)
    print("saved", name, logo.size)
