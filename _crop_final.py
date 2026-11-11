import os
from PIL import Image
TMP = r"samples\bank-render"; OUT = r"app\src\main\res\drawable-xxhdpi"

def crop(src, name, box):
    im = Image.open(os.path.join(TMP, src + ".png")).convert("RGB")
    c = im.crop(box); w, h = c.size
    out = Image.new("RGBA", (w, h), (0,0,0,0)); op = out.load(); cp = c.load()
    for y in range(h):
        for x in range(w):
            r,g,b = cp[x,y]
            if r < 245 or g < 245 or b < 245:
                op[x,y] = (r,g,b,255)
    bb = out.split()[3].getbbox()
    if bb: out = out.crop(bb)
    w2,h2 = out.size; s = max(w2,h2)
    canvas = Image.new("RGBA", (s,s), (0,0,0,0))
    canvas.paste(out, ((s-w2)//2,(s-h2)//2))
    pad = max(2,int(s*0.08))
    p = Image.new("RGBA",(s+pad*2,s+pad*2),(0,0,0,0)); p.paste(canvas,(pad,pad))
    p.resize((192,192), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))
    print(name, box, "->", w2, h2)

crop("pingan", "ic_bank_pingan", (0, 0, 96, 46))
crop("tenpay", "ic_bank_tenpay", (0, 0, 62, 54))
