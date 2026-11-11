import io, os, re

SRC = r"samples\logos"
DST = r"app\src\main\res\drawable"

HEAD = '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
'''
TAIL = "</vector>\n"

def convert(svg_file, out_name):
    svg = io.open(os.path.join(SRC, svg_file), encoding="utf-8").read()
    # simple-icons 是单条 fill path，viewBox 24x24
    m = re.search(r'<path d="([^"]+)"', svg)
    if not m:
        print("NO PATH", svg_file); return
    d = m.group(1)
    xml = HEAD + '    <path\n        android:fillColor="#FFFFFF"\n        android:pathData="%s" />\n' % d + TAIL
    io.open(os.path.join(DST, out_name + ".xml"), "w", encoding="utf-8", newline="\n").write(xml)
    print("wrote", out_name, len(d))

convert("wechat.svg", "ic_channel_wechat")
convert("alipay.svg", "ic_channel_alipay")
