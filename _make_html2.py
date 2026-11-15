import os, re, io

SRC = r"samples\payment-webfont-cn\svg"
HTML = r"samples\bank-html"
os.makedirs(HTML, exist_ok=True)

BANKS = ["icbc","boc","cmbchina","ccb","abchina","bankcomm","psbc","pingan","spd","unionpay","cncb"]

for b in BANKS:
    p = os.path.join(SRC, b + ".svg")
    if not os.path.exists(p): continue
    svg = io.open(p, encoding="utf-8").read()
    svg = re.sub(r"<\?xml[^>]*\?>", "", svg)
    # width/height 可能是纯数字、带 px、带百分号
    svg = re.sub(r'\swidth="[^"]*"', ' width="100%"', svg, count=1)
    svg = re.sub(r'\sheight="[^"]*"', ' height="100%"', svg, count=1)
    html = ('<!doctype html><html><head><meta charset="utf-8">'
            '<style>html,body{margin:0;padding:0;background:#fff}'
            'svg{display:block;width:100vw !important;height:100vh !important}</style></head><body>'
            + svg + '</body></html>')
    io.open(os.path.join(HTML, b + ".html"), "w", encoding="utf-8", newline="\n").write(html)
print("html regenerated")
