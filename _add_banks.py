import io

# 1) ChannelGlyph：加 bitmapIcons + 银行渠道映射
p = r"app\src\main\java\io\github\cyhkbl\within\ui\components\ChannelGlyph.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "bitmapIcons" not in s.split("private fun drawableFor")[0]:
    s = s.replace(
"""/** 有精确官方 logo 的渠道。 */
private fun drawableFor(channel: String?): Int? = when (channel) {""",
"""/** 用位图渲染的银行 logo（官方横排标志，显示时裁圆只留标志）。 */
private val bitmapIcons: Set<Int> = setOf(
    R.drawable.ic_bank_icbc, R.drawable.ic_bank_boc, R.drawable.ic_bank_cmb,
    R.drawable.ic_bank_ccb, R.drawable.ic_bank_abc, R.drawable.ic_bank_bocom,
    R.drawable.ic_bank_psbc, R.drawable.ic_bank_pingan, R.drawable.ic_bank_spdb,
    R.drawable.ic_bank_citic, R.drawable.ic_bank_unionpay
)

/** 有精确官方 logo 的渠道。 */
private fun drawableFor(channel: String?): Int? = when (channel) {
    "icbc" -> R.drawable.ic_bank_icbc
    "boc" -> R.drawable.ic_bank_boc
    "cmb" -> R.drawable.ic_bank_cmb
    "ccb" -> R.drawable.ic_bank_ccb
    "abc" -> R.drawable.ic_bank_abc
    "bocom" -> R.drawable.ic_bank_bocom
    "psbc" -> R.drawable.ic_bank_psbc
    "pingan" -> R.drawable.ic_bank_pingan
    "spdb" -> R.drawable.ic_bank_spdb
    "citic" -> R.drawable.ic_bank_citic""", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("glyph updated")

# 2) AccountChannels：加具体银行渠道
p = r"app\src\main\java\io\github\cyhkbl\within\data\AccountChannels.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if '"icbc"' not in s:
    s = s.replace(
'        AccountChannel("bank", "银行卡", FUND, 0xFF2E86DE),',
"""        AccountChannel("bank", "银行卡", FUND, 0xFF2E86DE),
        AccountChannel("icbc", "工商银行", FUND, 0xFFC7000B),
        AccountChannel("boc", "中国银行", FUND, 0xFFA6093D),
        AccountChannel("cmb", "招商银行", FUND, 0xFFD3001D),
        AccountChannel("ccb", "建设银行", FUND, 0xFF0055A5),
        AccountChannel("abc", "农业银行", FUND, 0xFF009B4C),
        AccountChannel("bocom", "交通银行", FUND, 0xFF005BAC),
        AccountChannel("psbc", "邮储银行", FUND, 0xFF008A5B),
        AccountChannel("pingan", "平安银行", FUND, 0xFFF36F21),
        AccountChannel("spdb", "浦发银行", FUND, 0xFF005BAC),
        AccountChannel("citic", "中信银行", FUND, 0xFFE60012),""", 1)
    # 迁移时优先识别具体银行
    s = s.replace(
'            // 银行一律归到银行卡，账户名本身保留\n            n.contains("银行") -> "bank"',
"""            n.contains("工商") -> "icbc"
            n.contains("中国银行") || n == "中行" -> "boc"
            n.contains("招商") -> "cmb"
            n.contains("建设") -> "ccb"
            n.contains("农业") -> "abc"
            n.contains("交通银行") -> "bocom"
            n.contains("邮储") || n.contains("邮政") -> "psbc"
            n.contains("平安") -> "pingan"
            n.contains("浦发") -> "spdb"
            n.contains("中信") -> "citic"
            n.contains("银行") -> "bank\"""", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("channels updated")
