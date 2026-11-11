import io, os, re

SRC = r"samples\logos"
DST = r"app\src\main\res\drawable"

# 文件 -> (输出名, 品牌色)
MAP = {
    "arcticons-wechat.svg": ("ic_channel_wechat", "#07C160"),
    "arcticons-alipay.svg": ("ic_channel_alipay", "#1677FF"),
    "arcticons-icbc.svg": ("ic_channel_icbc", "#C7000B"),
    "arcticons-boc.svg": ("ic_channel_boc", "#A6093D"),
    "arcticons-china-merchants-bank.svg": ("ic_channel_cmb", "#D3001D"),
    "arcticons-china-construction-bank.svg": ("ic_channel_ccb", "#0055A5"),
    "arcticons-agricultural-bank-of-china.svg": ("ic_channel_abc", "#009B4C"),
}

HEAD = '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="48"
    android:viewportHeight="48">
'''
TAIL = "</vector>\n"

def attr(attrs, name, default=None):
    # 前面不能紧跟字母/连字符，否则 id="x" 会被 d=" 匹配到
    m = re.search(r'(?<![\w-])%s="([^"]+)"' % name, attrs)
    return m.group(1) if m else default

def circle_to_path(attrs):
    cx = float(attr(attrs, "cx", 0)); cy = float(attr(attrs, "cy", 0)); r = float(attr(attrs, "r", 0))
    return "M%g,%g a%g,%g 0 1,0 %g,0 a%g,%g 0 1,0 -%g,0 Z" % (
        cx - r, cy, r, r, 2 * r, r, r, 2 * r)

def rect_to_path(attrs):
    x = float(attr(attrs, "x", 0)); y = float(attr(attrs, "y", 0))
    w = float(attr(attrs, "width", 0)); h = float(attr(attrs, "height", 0))
    rx = float(attr(attrs, "rx", 0) or 0); ry = float(attr(attrs, "ry", rx) or rx)
    if rx <= 0:
        return "M%g,%g h%g v%g h-%g Z" % (x, y, w, h, w)
    return ("M%g,%g h%g a%g,%g 0 0,1 %g,%g v%g a%g,%g 0 0,1 -%g,%g h-%g "
            "a%g,%g 0 0,1 -%g,-%g v-%g a%g,%g 0 0,1 %g,-%g Z") % (
        x + rx, y, w - 2 * rx, rx, ry, rx, ry, h - 2 * ry,
        rx, ry, rx, ry, w - 2 * rx, rx, ry, rx, ry, h - 2 * ry, rx, ry, rx, ry)

def shapes(svg):
    out = []
    for m in re.finditer(r"<(path|circle|rect)\b([^>]*?)/>", svg):
        tag, attrs = m.group(1), m.group(2)
        if tag == "path":
            d = attr(attrs, "d")
            if not d or not re.match(r"^[Mm]", d.strip()):
                continue
        elif tag == "circle":
            d = circle_to_path(attrs)
        else:
            d = rect_to_path(attrs)
        stroke = attr(attrs, "stroke")
        fill = attr(attrs, "fill")
        sw = attr(attrs, "stroke-width")
        out.append((d, stroke, fill, sw))
    return out

for src, (name, brand) in MAP.items():
    path = os.path.join(SRC, src)
    if not os.path.exists(path):
        print("MISS", src); continue
    svg = io.open(path, encoding="utf-8").read()
    items = shapes(svg)
    if not items:
        print("NO SHAPE", src); continue

    body = []
    for d, stroke, fill, sw in items:
        is_stroke = stroke is not None or fill == "none"
        if is_stroke:
            width = float(sw) if sw else 2.2
            attrs = ['android:strokeColor="%s"' % brand,
                     'android:strokeWidth="%s"' % width,
                     'android:strokeLineCap="round"',
                     'android:strokeLineJoin="round"',
                     'android:fillColor="#00000000"']
        else:
            attrs = ['android:fillColor="%s"' % brand]
        body.append('    <path\n        %s\n        android:pathData="%s" />\n'
                    % ("\n        ".join(attrs), d))

    io.open(os.path.join(DST, name + ".xml"), "w", encoding="utf-8", newline="\n").write(HEAD + "".join(body) + TAIL)
    print("wrote", name, brand, "shapes=", len(items))
