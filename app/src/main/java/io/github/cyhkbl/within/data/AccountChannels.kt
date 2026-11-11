package io.github.cyhkbl.within.data

/**
 * 账户的「渠道」。
 *
 * 记账人的心智是「钱在微信里」「钱在工商银行」，而不是「这是一个钱包类账户」，
 * 所以账户类型直接就是渠道本身。分组沿用钱迹的三段：资金 / 信用 / 充值。
 * 同名账户再次新建时自动加序号（微信 → 微信2 → 微信3）。
 */
data class AccountChannel(
    val key: String,
    val label: String,
    val group: String,     // FUND 资金 / CREDIT 信用 / RECHARGE 充值
    val brand: Long        // ARGB 品牌色
)

object AccountChannels {

    const val FUND = "FUND"
    const val CREDIT = "CREDIT"
    const val RECHARGE = "RECHARGE"

    val all: List<AccountChannel> = listOf(
        // ── 资金账户 ──
        AccountChannel("cash", "现金", FUND, 0xFF6C5CE7),
        AccountChannel("wechat", "微信", FUND, 0xFF07C160),
        AccountChannel("wechat_change", "微信零钱通", FUND, 0xFFF5B301),
        AccountChannel("alipay", "支付宝", FUND, 0xFF1677FF),
        AccountChannel("yuebao", "余额宝", FUND, 0xFFFF6B35),
        AccountChannel("yulibao", "余利宝", FUND, 0xFF2E86FF),
        AccountChannel("bank", "银行卡", FUND, 0xFF2E86DE),
        AccountChannel("icbc", "工商银行", FUND, 0xFFC7000B),
        AccountChannel("boc", "中国银行", FUND, 0xFFA6093D),
        AccountChannel("cmb", "招商银行", FUND, 0xFFD3001D),
        AccountChannel("ccb", "建设银行", FUND, 0xFF0055A5),
        AccountChannel("abc", "农业银行", FUND, 0xFF009B4C),
        AccountChannel("bocom", "交通银行", FUND, 0xFF005BAC),
        AccountChannel("qq", "QQ 钱包", FUND, 0xFF12B7F5),
        AccountChannel("other", "其它", FUND, 0xFF7A4FCF),

        // ── 信用卡账户 ──
        AccountChannel("credit", "信用卡", CREDIT, 0xFFFF7043),
        AccountChannel("huabei", "花呗", CREDIT, 0xFF1677FF),
        AccountChannel("jiebei", "借呗", CREDIT, 0xFF1677FF),
        AccountChannel("baitiao", "京东白条", CREDIT, 0xFFE1251B),

        // ── 充值账户 ──
        AccountChannel("phone", "话费", RECHARGE, 0xFFFF6B35),
        AccountChannel("utility", "水电", RECHARGE, 0xFF16A085),
        AccountChannel("meal_card", "饭卡", RECHARGE, 0xFFF39C12),
        AccountChannel("deposit", "押金", RECHARGE, 0xFF3498DB)
    )

    val fundChannels get() = all.filter { it.group == FUND }
    val creditChannels get() = all.filter { it.group == CREDIT }
    val rechargeChannels get() = all.filter { it.group == RECHARGE }

    fun byKey(key: String?): AccountChannel? = all.firstOrNull { it.key == key }

    fun labelOf(key: String?): String = byKey(key)?.label ?: "其它"

    fun groupLabel(group: String): String = when (group) {
        CREDIT -> "信用卡账户"
        RECHARGE -> "充值账户"
        else -> "资金账户"
    }

    /**
     * 老数据迁移：按账户名猜渠道。
     */
    fun inferFromName(name: String, legacyType: String?): String {
        val n = name.trim()
        return when {
            n.contains("零钱通") -> "wechat_change"
            n.contains("微信") -> "wechat"
            n.contains("余额宝") -> "yuebao"
            n.contains("余利宝") -> "yulibao"
            n.contains("支付宝") -> "alipay"
            n.contains("花呗") -> "huabei"
            n.contains("借呗") -> "jiebei"
            n.contains("白条") -> "baitiao"
            n.contains("QQ") || n.contains("qq") -> "qq"
            n.contains("工商") -> "icbc"
            n.contains("中国银行") || n == "中行" -> "boc"
            n.contains("招商") -> "cmb"
            n.contains("建设") -> "ccb"
            n.contains("农业") -> "abc"
            n.contains("交通银行") -> "bocom"
            n.contains("信用") || legacyType == "CREDIT" -> "credit"
            n.contains("现金") || legacyType == "CASH" -> "cash"
            n.contains("话费") -> "phone"
            n.contains("水电") -> "utility"
            n.contains("饭卡") -> "meal_card"
            n.contains("押金") -> "deposit"
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
