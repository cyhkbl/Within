import io, os, re
D = r"app\src\main\res\drawable"
COLORS = {"ic_channel_cash": "#00875F", "ic_channel_credit": "#7A4FCF", "ic_channel_other": "#5C6470"}
for name, brand in COLORS.items():
    p = os.path.join(D, name + ".xml")
    s = io.open(p, encoding="utf-8").read()
    s = s.replace('android:fillColor="#FFFFFF"', 'android:fillColor="%s"' % brand)
    s = s.replace('android:strokeColor="#FFFFFF"', 'android:strokeColor="%s"' % brand)
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("recoloured", name, brand)
