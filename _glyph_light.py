import io

# 1) 银行位图改用浅色底，避免品牌色底淹没彩色 logo
p = r"app\src\main\java\io\github\cyhkbl\within\ui\components\ChannelGlyph.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""@Composable
fun ChannelGlyph(channel: String?, size: Dp = 44.dp) {
    val spec = AccountChannels.byKey(channel)
    val brand = Color(spec?.brand ?: 0xFF7A4FCF)
    val drawable = drawableFor(channel)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(brand),
        contentAlignment = Alignment.Center
    ) {""",
"""@Composable
fun ChannelGlyph(channel: String?, size: Dp = 44.dp) {
    val spec = AccountChannels.byKey(channel)
    val brand = Color(spec?.brand ?: 0xFF7A4FCF)
    val drawable = drawableFor(channel)
    val isBitmapLogo = drawable != null && drawable in bitmapIcons
    // 银行 logo 本身就是彩色的，底再上品牌色就看不清图形了，给个浅色托底
    val container = if (isBitmapLogo) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        brand
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {""")

s = s.replace("""        if (drawable != null) {
            val isBitmap = drawable in bitmapIcons
            Icon(
                painter = painterResource(drawable),
                contentDescription = null,
                tint = if (isBitmap) Color.Unspecified else Color.White,
                modifier = Modifier
                    .size(if (isBitmap) size * 0.72f else size * 0.58f)
                    // 位图 logo 是「标志 + 文字」的横排，裁成圆形只留标志
                    .then(if (isBitmap) Modifier.clip(CircleShape) else Modifier)
            )""",
"""        if (drawable != null) {
            Icon(
                painter = painterResource(drawable),
                contentDescription = null,
                tint = if (isBitmapLogo) Color.Unspecified else Color.White,
                modifier = Modifier
                    .size(if (isBitmapLogo) size * 0.78f else size * 0.58f)
            )""")

if "MaterialTheme" not in s.split("import")[0]:
    if "import androidx.compose.material3.MaterialTheme" not in s:
        s = s.replace("import androidx.compose.material3.Icon",
                      "import androidx.compose.material3.Icon\nimport androidx.compose.material3.MaterialTheme", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("ChannelGlyph updated")
