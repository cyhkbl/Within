package io.github.cyhkbl.within.data

/**
 * 账户的「渠道」。
 *
 * 记账人的心智是「钱在微信里」「钱在工商银行」，而不是「这是一个钱包类账户」，
 * 所以账户类型直接就是渠道本身：微信、支付宝、工商银行……
 * 同名账户再次新建时自动加序号（微信 → 微信2 → 微信3）。
 */
data class AccountChannel(
    val key: String,
    val label: String,
    val icon: String,      // 图标标识，用于在 ui 层映射到具体图形
    val group: String      // FUND 资金 / CREDIT 信用
)

object AccountChannels {

    val all: List<AccountChannel> = listOf(
        AccountChannel("cash", "现金", "cash", "FUND"),
        AccountChannel("wechat", "微信", "wechat", "FUND"),
        AccountChannel("alipay", "支付宝", "alipay", "FUND"),
        AccountChannel("icbc", "工商银行", "icbc", "FUND"),
        AccountChannel("boc", "中国银行", "boc", "FUND"),
        AccountChannel("cmb", "招商银行", "cmb", "FUND"),
        AccountChannel("ccb", "建设银行", "ccb", "FUND"),
        AccountChannel("abc", "农业银行", "abc", "FUND"),
        AccountChannel("bocom", "交通银行", "bocom", "FUND"),
        AccountChannel("credit", "信用卡", "credit", "CREDIT"),
        AccountChannel("other", "其它", "other", "FUND")
    )

    fun byKey(key: String?): AccountChannel? = all.firstOrNull { it.key == key }

    fun labelOf(key: String?): String = byKey(key)?.label ?: "其它"

    /**
     * 老数据迁移：按账户名猜渠道。之前 type 存的是 CASH/WALLET/BANK/CREDIT/INVEST，
     * 这里把名字里带的机构识别出来，识别不出就落到其它。
     */
    fun inferFromName(name: String, legacyType: String?): String {
        val n = name.trim()
        return when {
            n.contains("微信") -> "wechat"
            n.contains("支付宝") || n.contains("余额宝") -> "alipay"
            n.contains("工商") -> "icbc"
            n.contains("中国银行") || n == "中行" -> "boc"
            n.contains("招商") -> "cmb"
            n.contains("建设") -> "ccb"
            n.contains("农业") -> "abc"
            n.contains("交通银行") -> "bocom"
            n.contains("信用") || legacyType == "CREDIT" -> "credit"
            n.contains("现金") || legacyType == "CASH" -> "cash"
            legacyType == "WALLET" -> "other"
            else -> "other"
        }
    }

    /**
     * 新建同渠道账户时的默认名字：第一个用渠道名，之后依次加序号。
     * 「微信」之后新建就是「微信2」「微信3」。
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
