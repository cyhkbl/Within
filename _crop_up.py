import os
from PIL import Image
TMP = r"samples\bank-render"
OUT = r"app\src\main\res\drawable-xxhdpi"

def is_ink(r, g, b):
    return r < 245 or g < 245 or b < 245

# 银联是横向方块，保留原比例，不裁成正方形
im = Image.open(os.path.join(TMP, "unionpay@big.png")).convert("RGB")
W, H = im.size
px = im.load()
minx, miny, maxx, maxy = W, H, 0, 0
for y in range(0, H, 2):
    for x in range(0, W, 2):
        if is_ink(*px[x, y]):
            minx = min(minx, x); maxx = max(maxx, x)
            miny = min(miny, y); maxy = max(maxy, y)

crop = im.crop((minx, miny, maxx + 1, maxy + 1))
cw, ch = crop.size
out = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
op = out.load(); cp = crop.load()
for y in range(ch):
    for x in range(cw):
        r, g, b = cp[x, y]
        if is_ink(r, g, b):
            op[x, y] = (r, g, b, 255)
out.resize((256, int(256 * ch / cw)), Image.LANCZOS).save(os.path.join(OUT, "ic_bank_unionpay.png"))
print("unionpay", cw, ch)
