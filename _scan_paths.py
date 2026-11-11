import io, re, os
D = r"app\src\main\res\drawable"
for f in sorted(os.listdir(D)):
    if not f.startswith("ic_channel_"):
        continue
    s = io.open(os.path.join(D, f), encoding="utf-8").read()
    for m in re.finditer(r'android:pathData="([^"]*)"', s):
        d = m.group(1)
        if not re.match(r'^[Mm]', d):
            print("BAD", f, "->", repr(d[:40]))
        elif not re.search(r'\d', d):
            print("NODIGIT", f, "->", repr(d[:40]))
print("scan done")
