package io.github.cyhkbl.within.data

/**
 * 账户的「渠道」。
 *
 * 分组沿用钱迹的五段：资金 / 信用卡 / 充值 / 投资理财 / 债务。
 * 同名账户再次新建时自动加序号（微信 → 微信2 → 微信3）。
 */
data class AccountChannel(
    val key: String,
    val label: String,
    val group: String,     // FUND / CREDIT / RECHARGE / INVEST / DEBT
    val brand: Long,       // ARGB 品牌色
    val parent: String? = null   // 非空表示是某个渠道的子项（如「银行卡」下的具体银行）
)

object AccountChannels {

    const val FUND = "FUND"
    const val CREDIT = "CREDIT"
    const val RECHARGE = "RECHARGE"
    const val INVEST = "INVEST"
    const val DEBT = "DEBT"

    val all: List<AccountChannel> = listOf(
        // ── 资金账户 ──
        AccountChannel("cash", "现金", FUND, 0xFF6C5CE7),
        AccountChannel("wechat", "微信", FUND, 0xFF07C160),
        AccountChannel("wechat_change", "微信零钱通", FUND, 0xFFF5B301),
        AccountChannel("alipay", "支付宝", FUND, 0xFF1677FF),
        AccountChannel("yuebao", "余额宝", FUND, 0xFFFF6B35),
        AccountChannel("yulibao", "余利宝", FUND, 0xFF2E86FF),
        AccountChannel("purse", "小荷包", FUND, 0xFFFF4757),
        AccountChannel("yunshanfu", "云闪付", FUND, 0xFFE1251B),
        AccountChannel("bank", "银行卡", FUND, 0xFF2E86DE),
        AccountChannel("icbc", "工商银行", FUND, 0xFFC7000B, "bank"),
        AccountChannel("boc", "中国银行", FUND, 0xFFA6093D, "bank"),
        AccountChannel("cmb", "招商银行", FUND, 0xFFD3001D, "bank"),
        AccountChannel("ccb", "建设银行", FUND, 0xFF0055A5, "bank"),
        AccountChannel("abc", "农业银行", FUND, 0xFF009B4C, "bank"),
        AccountChannel("bocom", "交通银行", FUND, 0xFF005BAC, "bank"),
        AccountChannel("psbc", "邮储银行", FUND, 0xFF008A5B, "bank"),
        AccountChannel("pingan", "平安银行", FUND, 0xFFF36F21, "bank"),
        AccountChannel("spdb", "浦发银行", FUND, 0xFF005BAC, "bank"),
        AccountChannel("citic", "中信银行", FUND, 0xFFE60012, "bank"),
        AccountChannel("housing_fund", "公积金", FUND, 0xFF7A4FCF),
        AccountChannel("qq", "QQ 钱包", FUND, 0xFF12B7F5),
        AccountChannel("jd", "京东金融", FUND, 0xFFE1251B),
        AccountChannel("medical", "医保", FUND, 0xFFA29BFE),
        AccountChannel("digital_cny", "数字人民币", FUND, 0xFFE1251B),
        AccountChannel("huawei", "华为钱包", FUND, 0xFFE1251B),
        AccountChannel("pdd", "多多钱包", FUND, 0xFFE1251B),
        AccountChannel("paypal", "Paypal", FUND, 0xFF003087),
        AccountChannel("other", "其它", FUND, 0xFF7A4FCF),

        // ── 信用卡账户 ──
        AccountChannel("credit", "信用卡", CREDIT, 0xFFFF7043),
        AccountChannel("huabei", "花呗", CREDIT, 0xFF1677FF),
        AccountChannel("jiebei", "借呗", CREDIT, 0xFF1677FF),
        AccountChannel("baitiao", "京东白条", CREDIT, 0xFFE1251B),
        AccountChannel("meituan", "美团月付", CREDIT, 0xFFFFC107),
        AccountChannel("douyin", "抖音月付", CREDIT, 0xFF00E5D0),
        AccountChannel("wechat_fenqi", "微信分付", CREDIT, 0xFF07C160),
        AccountChannel("other_credit", "其它信用卡", CREDIT, 0xFF8D99AE),

        // ── 充值账户 ──
        AccountChannel("phone", "话费", RECHARGE, 0xFFFF6B35),
        AccountChannel("utility", "水电", RECHARGE, 0xFF16A085),
        AccountChannel("meal_card", "饭卡", RECHARGE, 0xFFF39C12),
        AccountChannel("deposit", "押金", RECHARGE, 0xFF3498DB),
        AccountChannel("bus_card", "公交卡", RECHARGE, 0xFF607D8B),
        AccountChannel("member_card", "会员卡", RECHARGE, 0xFFFF7043),
        AccountChannel("fuel_card", "加油卡", RECHARGE, 0xFF8E44AD),
        AccountChannel("sinopec", "石化钱包", RECHARGE, 0xFFE1251B),
        AccountChannel("apple", "Apple", RECHARGE, 0xFF333333),
        AccountChannel("other_recharge", "其它充值卡", RECHARGE, 0xFF3498DB),

        // ── 投资理财 ──
        AccountChannel("stock", "股票", INVEST, 0xFFFF6B35),
        AccountChannel("fund", "基金", INVEST, 0xFFFF7043),
        AccountChannel("gold", "黄金", INVEST, 0xFFF5B301),
        AccountChannel("forex", "外汇", INVEST, 0xFF1677FF),
        AccountChannel("futures", "期货", INVEST, 0xFFE1251B),
        AccountChannel("bond", "债券", INVEST, 0xFF3498DB),
        AccountChannel("fixed_income", "固定收益", INVEST, 0xFF6C5CE7),
        AccountChannel("crypto", "加密货币", INVEST, 0xFFF7931A),
        AccountChannel("other_invest", "其它理财", INVEST, 0xFF1677FF),

        // ── 债务 ──
        AccountChannel("lend_out", "借出", DEBT, 0xFFFF4757),
        AccountChannel("borrow_in", "借入", DEBT, 0xFF2ED573)
    )

    /** 一级渠道（有子项的渠道不直接展开）。 */
    val topLevel: List<AccountChannel> get() = all.filter { it.parent == null }

    /** 某个渠道下的子项，比如「银行卡」下的各家银行。 */
    fun childrenOf(key: String): List<AccountChannel> = all.filter { it.parent == key }

    fun hasChildren(key: String): Boolean = all.any { it.parent == key }

    val fundChannels get() = all.filter { it.group == FUND && it.parent == null }
    val creditChannels get() = all.filter { it.group == CREDIT && it.parent == null }
    val rechargeChannels get() = all.filter { it.group == RECHARGE && it.parent == null }
    val investChannels get() = all.filter { it.group == INVEST && it.parent == null }
    val debtChannels get() = all.filter { it.group == DEBT && it.parent == null }

    fun byKey(key: String?): AccountChannel? = all.firstOrNull { it.key == key }

    fun labelOf(key: String?): String = byKey(key)?.label ?: "其它"

    fun groupLabel(group: String): String = when (group) {
        CREDIT -> "信用卡账户"
        RECHARGE -> "充值账户"
        INVEST -> "投资理财账户"
        DEBT -> "债务"
        else -> "资金账户"
    }

    /**
     * 老数据迁移：按账户名猜渠道。
     */
    fun inferFromName(name: String, legacyType: String?): String {
        val n = name.trim()
        return when {
            n.contains("零钱通") -> "wechat_change"
            n.contains("微信") || n.contains("分付") && n.contains("微信") -> "wechat"
            n.contains("余额宝") -> "yuebao"
            n.contains("余利宝") -> "yulibao"
            n.contains("花呗") -> "huabei"
            n.contains("借呗") -> "jiebei"
            n.contains("白条") -> "baitiao"
            n.contains("支付宝") -> "alipay"
            n.contains("云闪付") -> "yunshanfu"
            n.contains("小荷包") -> "purse"
            n.contains("公积金") -> "housing_fund"
            n.contains("医保") -> "medical"
            n.contains("数字人民币") -> "digital_cny"
            n.contains("华为") -> "huawei"
            n.contains("多多") -> "pdd"
            n.contains("Paypal") || n.contains("paypal") -> "paypal"
            n.contains("QQ") || n.contains("qq") -> "qq"
            n.contains("京东金融") -> "jd"
            n.contains("股票") -> "stock"
            n.contains("基金") -> "fund"
            n.contains("黄金") -> "gold"
            n.contains("外汇") -> "forex"
            n.contains("期货") -> "futures"
            n.contains("债券") -> "bond"
            n.contains("加密") -> "crypto"
            n.contains("话费") -> "phone"
            n.contains("水电") -> "utility"
            n.contains("饭卡") -> "meal_card"
            n.contains("押金") -> "deposit"
            n.contains("公交") -> "bus_card"
            n.contains("会员") -> "member_card"
            n.contains("加油") -> "fuel_card"
            n.contains("Apple") || n.contains("apple") -> "apple"
            n.contains("工商") -> "icbc"
            n.contains("中国银行") || n == "中行" -> "boc"
            n.contains("招商") -> "cmb"
            n.contains("建设") -> "ccb"
            n.contains("农业") -> "abc"
            n.contains("交通银行") -> "bocom"
            n.contains("邮储") || n.contains("邮政") -> "psbc"
            n.contains("平安") -> "pingan"
            n.contains("浦发") -> "spdb"
            n.contains("中信") -> "citic"
            n.contains("银行") -> "bank"
            n.contains("信用") || legacyType == "CREDIT" -> "credit"
            n.contains("现金") || legacyType == "CASH" -> "cash"
            else -> "other"
        }
    }

    /**
     * 新建同渠道账户时的默认名字：第一个用渠道名，之后依次加序号。
     */
    fun nextName(channelKey: String, existing: List<String>): String {
        val base = labelOf(channelKey)
        val taken = existing.toSet()
        if (!taken.contains(base)) return base
        var index = 2
        while (taken.contains("$base$index")) index++
        return "$base$index"
    }
}
