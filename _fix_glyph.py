import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\components\ChannelGlyph.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """        val res = skin.iconRes
        if (res != null) {
            Icon(
                painter = painterResource(res),
                contentDescription = null,
                tint = skin.brand,
                modifier = Modifier.size(size * 0.62f)
            )"""
new = """        val res = skin.iconRes
        if (res != null) {
            // 图标本身已经烤进品牌色，这里不能再 tint，否则会被主题色盖掉
            Icon(
                painter = painterResource(res),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(size * 0.62f)
            )"""
if old in s:
    s = s.replace(old, new, 1); print("glyph OK")
else:
    print("glyph MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
