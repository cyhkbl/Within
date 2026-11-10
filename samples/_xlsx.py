import zipfile, re
from xml.etree import ElementTree as ET

path = r"samples\qianji.xlsx"
z = zipfile.ZipFile(path)
print("entries:", [n for n in z.namelist()][:8])

ns = {"m": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}

# shared strings
shared = []
if "xl/sharedStrings.xml" in z.namelist():
    root = ET.fromstring(z.read("xl/sharedStrings.xml"))
    for si in root.findall("m:si", ns):
        shared.append("".join(t.text or "" for t in si.iter("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}t")))
print("shared strings:", len(shared), "first 5:", shared[:5])

# sheet
sheet_name = "xl/worksheets/sheet1.xml"
root = ET.fromstring(z.read(sheet_name))
rows = root.findall(".//m:sheetData/m:row", ns)
print("rows:", len(rows))

def cell_value(c):
    t = c.get("t")
    v = c.find("m:v", ns)
    if t == "s":
        return shared[int(v.text)] if v is not None else ""
    if t == "inlineStr":
        is_ = c.find("m:is", ns)
        return "".join(x.text or "" for x in is_.iter("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}t")) if is_ is not None else ""
    return v.text if v is not None else ""

for row in rows[:3]:
    cells = {c.get("r"): cell_value(c) for c in row.findall("m:c", ns)}
    print(sorted(cells.items())[:20])
