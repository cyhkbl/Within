import io, os, re
D = r"app\src\main\res\drawable"
# 图标统一成白色，靠实心品牌色圆底衬托
for f in sorted(os.listdir(D)):
    if not f.startswith("ic_channel_"):
        continue
    p = os.path.join(D, f)
    s = io.open(p, encoding="utf-8").read()
    s = re.sub(r'android:(strokeColor|fillColor)="#[0-9A-Fa-f]{6}"', lambda m: 'android:%s="#FFFFFF"' % m.group(1), s)
    s = s.replace('android:fillColor="#00000000"', 'android:fillColor="#00000000"')
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("white:", f)
