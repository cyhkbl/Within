import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\components\ChannelGlyph.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(skin.brand.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {"""
new = """    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(skin.brand),
        contentAlignment = Alignment.Center
    ) {"""
if old in s:
    s = s.replace(old, new, 1); print("solid circle OK")
else:
    print("MISS circle")

old2 = """            Icon(
                painter = painterResource(res),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(size * 0.62f)
            )"""
new2 = """            Icon(
                painter = painterResource(res),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.58f)
            )"""
if old2 in s:
    s = s.replace(old2, new2, 1); print("white icon OK")
else:
    print("MISS icon")

old3 = """            SymbolText(
                name = skin.symbol ?: "account_balance",
                size = size * 0.52f,
                color = skin.brand
            )"""
new3 = """            SymbolText(
                name = skin.symbol ?: "account_balance",
                size = size * 0.5f,
                color = Color.White
            )"""
if old3 in s:
    s = s.replace(old3, new3, 1); print("symbol OK")
else:
    print("MISS symbol")
io.open(p, "w", encoding="utf-8", newline="").write(s)
