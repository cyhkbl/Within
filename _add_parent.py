import io

# 1) AccountChannels: 加 parent，银行挂到 bank 下面
p = r"app\src\main\java\io\github\cyhkbl\within\data\AccountChannels.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""data class AccountChannel(
    val key: String,
    val label: String,
    val group: String,     // FUND / CREDIT / RECHARGE / INVEST / DEBT
    val brand: Long        // ARGB 品牌色
)""",
"""data class AccountChannel(
    val key: String,
    val label: String,
    val group: String,     // FUND / CREDIT / RECHARGE / INVEST / DEBT
    val brand: Long,       // ARGB 品牌色
    val parent: String? = null   // 非空表示是某个渠道的子项（如「银行卡」下的具体银行）
)""")

# 银行改成 bank 的子项
s = s.replace('        AccountChannel("icbc", "工商银行", FUND, 0xFFC7000B),', '        AccountChannel("icbc", "工商银行", FUND, 0xFFC7000B, "bank"),')
s = s.replace('        AccountChannel("boc", "中国银行", FUND, 0xFFA6093D),', '        AccountChannel("boc", "中国银行", FUND, 0xFFA6093D, "bank"),')
s = s.replace('        AccountChannel("cmb", "招商银行", FUND, 0xFFD3001D),', '        AccountChannel("cmb", "招商银行", FUND, 0xFFD3001D, "bank"),')
s = s.replace('        AccountChannel("ccb", "建设银行", FUND, 0xFF0055A5),', '        AccountChannel("ccb", "建设银行", FUND, 0xFF0055A5, "bank"),')
s = s.replace('        AccountChannel("abc", "农业银行", FUND, 0xFF009B4C),', '        AccountChannel("abc", "农业银行", FUND, 0xFF009B4C, "bank"),')
s = s.replace('        AccountChannel("bocom", "交通银行", FUND, 0xFF005BAC),', '        AccountChannel("bocom", "交通银行", FUND, 0xFF005BAC, "bank"),')
s = s.replace('        AccountChannel("psbc", "邮储银行", FUND, 0xFF008A5B),', '        AccountChannel("psbc", "邮储银行", FUND, 0xFF008A5B, "bank"),')
s = s.replace('        AccountChannel("pingan", "平安银行", FUND, 0xFFF36F21),', '        AccountChannel("pingan", "平安银行", FUND, 0xFFF36F21, "bank"),')
s = s.replace('        AccountChannel("spdb", "浦发银行", FUND, 0xFF005BAC),', '        AccountChannel("spdb", "浦发银行", FUND, 0xFF005BAC, "bank"),')
s = s.replace('        AccountChannel("citic", "中信银行", FUND, 0xFFE60012),', '        AccountChannel("citic", "中信银行", FUND, 0xFFE60012, "bank"),')

# 顶层 / 子级访问器
s = s.replace("""    val fundChannels get() = all.filter { it.group == FUND }""",
"""    /** 一级渠道（有子项的渠道不直接展开）。 */
    val topLevel: List<AccountChannel> get() = all.filter { it.parent == null }

    /** 某个渠道下的子项，比如「银行卡」下的各家银行。 */
    fun childrenOf(key: String): List<AccountChannel> = all.filter { it.parent == key }

    fun hasChildren(key: String): Boolean = all.any { it.parent == key }

    val fundChannels get() = all.filter { it.group == FUND && it.parent == null }""")

s = s.replace("""    val creditChannels get() = all.filter { it.group == CREDIT }""",
"""    val creditChannels get() = all.filter { it.group == CREDIT && it.parent == null }""")
s = s.replace("""    val rechargeChannels get() = all.filter { it.group == RECHARGE }""",
"""    val rechargeChannels get() = all.filter { it.group == RECHARGE && it.parent == null }""")
s = s.replace("""    val investChannels get() = all.filter { it.group == INVEST }""",
"""    val investChannels get() = all.filter { it.group == INVEST && it.parent == null }""")
s = s.replace("""    val debtChannels get() = all.filter { it.group == DEBT }""",
"""    val debtChannels get() = all.filter { it.group == DEBT && it.parent == null }""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("channels parent OK")
