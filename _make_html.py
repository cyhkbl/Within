import os, re, io

SRC = r"samples\payment-webfont-cn\svg"
HTML = r"samples\bank-html"
os.makedirs(HTML, exist_ok=True)

BANKS = ["icbc","boc","cmbchina","ccb","abchina","bankcomm","psbc","pingan","spd","unionpay","cncb"]

for b in BANKS:
    p = os.path.join(SRC, b + ".svg")
    if not os.path.exists(p): continue
    svg = io.open(p, encoding="utf-8").read()
    # 去掉 xml 声明，把固有尺寸换成百分比，让 viewBox 撑满容器
    svg = re.sub(r"<\?xml[^>]*\?>", "", svg)
    svg = re.sub(r'\swidth="[\d.]+px?"', ' width="100%"', svg, count=1)
    svg = re.sub(r'\sheight="[\d.]+px?"', ' height="100%"', svg, count=1)
    svg = re.sub(r'style="[^"]*width[^"]*"', "", svg)
    html = ('<!doctype html><html><head><meta charset="utf-8">'
            '<style>html,body{margin:0;padding:0;background:#fff}'
            'svg{display:block;width:100vw;height:100vh}</style></head><body>'
            + svg + '</body></html>')
    io.open(os.path.join(HTML, b + ".html"), "w", encoding="utf-8", newline="\n").write(html)
    print("html", b)
