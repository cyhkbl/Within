import re, sys
from xml.etree import ElementTree as ET
path = sys.argv[1] if len(sys.argv) > 1 else r"samples\ui.xml"
tree = ET.parse(path)
for node in tree.iter():
    text = (node.get("text") or "").strip()
    desc = (node.get("content-desc") or "").strip()
    label = text or desc
    if not label:
        continue
    b = node.get("bounds") or ""
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", b)
    if not m:
        continue
    x1, y1, x2, y2 = map(int, m.groups())
    print(f"{label!r:50s} center=({(x1+x2)//2},{(y1+y2)//2})")
