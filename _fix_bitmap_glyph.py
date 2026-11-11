import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\components\ChannelGlyph.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

old = """        if (drawable != null) {
            Icon(
                painter = painterResource(drawable),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.58f)
            )"""
new = """        if (drawable != null) {
            val isBitmap = drawable in bitmapIcons
            Icon(
                painter = painterResource(drawable),
                contentDescription = null,
                tint = if (isBitmap) Color.Unspecified else Color.White,
                modifier = Modifier
                    .size(if (isBitmap) size * 0.72f else size * 0.58f)
                    // 位图 logo 是「标志 + 文字」的横排，裁成圆形只留标志
                    .then(if (isBitmap) Modifier.clip(CircleShape) else Modifier)
            )"""
if old in s:
    s = s.replace(old, new, 1); print("glyph OK")
else:
    print("MISS glyph")

# 银行位图集合
if "bitmapIcons" not in s:
    s = s.replace(
"""/** 有精确官方 logo 的渠道。 */
private fun drawableFor(channel: String?): Int? = when (channel) {""",
"""/** 用位图渲染的银行 logo（标志 + 文字，显示时裁圆）。 */
private val bitmapIcons: Set<Int> = setOf(
    R.drawable.ic_bank_icbc, R.drawable.ic_bank_boc, R.drawable.ic_bank_cmb,
    R.drawable.ic_bank_ccb, R.drawable.ic_bank_abc, R.drawable.ic_bank_bocom,
    R.drawable.ic_bank_psbc, R.drawable.ic_bank_pingan, R.drawable.ic_bank_spdb,
    R.drawable.ic_bank_unionpay, R.drawable.ic_bank_citic, R.drawable.ic_bank_tenpay
)

/** 有精确官方 logo 的渠道。 */
private fun drawableFor(channel: String?): Int? = when (channel) {""", 1)

# 银行卡渠道映射到具体银行
s = s.replace('''    "bank" -> "credit_card"''', '''    "bank" -> "credit_card"''')

io.open(p, "w", encoding="utf-8", newline="").write(s)
