import zipfile, re, io
p = r"samples\qianji-new.xlsx"
z = zipfile.ZipFile(p)
names = z.namelist()
print("entries:", [n for n in names if n.endswith(".xml")][:8])
# 读共享字符串数量
try:
    ss = z.read("xl/sharedStrings.xml").decode("utf-8")
    count = ss.count("<si>")
    print("sharedStrings count:", count)
except KeyError:
    print("no sharedStrings")
# 读第一张表
sheet = z.read("xl/worksheets/sheet1.xml").decode("utf-8")
rows = re.findall(r"<row ", sheet)
print("row count:", len(rows))
