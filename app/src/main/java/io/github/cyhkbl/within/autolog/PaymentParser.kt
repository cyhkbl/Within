package io.github.cyhkbl.within.autolog

import java.security.MessageDigest

data class PaymentDraft(
    val amount: Long,              // 分
    val kind: String,              // EXPENSE / INCOME
    val merchant: String,
    val accountHint: String,       // 微信 / 支付宝 / 淘宝 …
    val sourcePackage: String,
    val rawText: String,
    val timestamp: Long,
    val fingerprint: String,
    val confidence: Float = 0.8f
)

/**
 * 从通知标题/正文和页面文本里抽金额、判断收支、猜商户。
 * 这里刻意只用可解释的规则，不做黑盒判断——记错账比不记更烦。
 */
object PaymentParser {

    private val amountPatterns = listOf(
        Regex("""[¥￥]\s*([0-9][0-9,]*\.?[0-9]{0,2})"""),
        Regex("""([0-9][0-9,]*\.?[0-9]{0,2})\s*元"""),
        Regex("""(?:金额|实付|付款|支付|消费)[^0-9]{0,6}([0-9][0-9,]*\.?[0-9]{0,2})""")
    )

    private val incomeKeywords = listOf(
        "收款", "到账", "退款", "已退", "转入", "收入", "红包已领取"
    )

    private val expenseKeywords = listOf(
        "支付成功", "付款成功", "支付", "付款", "消费", "扣款", "转出", "扣费", "交易成功"
    )

    private val noiseKeywords = listOf(
        "失败", "已取消", "取消支付", "待支付", "未支付", "余额不足", "验证码", "排队中"
    )

    private val merchantPatterns = listOf(
        Regex("""[向给]\s*([^\s，,。:：]{1,20})\s*(?:付款|转账)"""),
        Regex("""在\s*([^\s，,。:：]{1,20})\s*(?:消费|购买)"""),
        Regex("""商户[名称]*[:：]\s*([^\s，,。:：]{1,30})"""),
        Regex("""(?:收款方|对方)[:：]\s*([^\s，,。:：]{1,20})""")
    )

    fun parseNotification(packageName: String, title: String, text: String): PaymentDraft? {
        val content = "$title $text".trim()
        if (content.isBlank()) return null
        if (noiseKeywords.any { content.contains(it) }) return null

        val amount = extractAmount(content) ?: return null
        if (amount <= 0) return null

        val isIncome = incomeKeywords.any { content.contains(it) }
        val hasExpense = expenseKeywords.any { content.contains(it) }
        if (!isIncome && !hasExpense) return null

        val kind = if (isIncome) "INCOME" else "EXPENSE"
        val merchant = extractMerchant(content) ?: defaultMerchant(packageName, title)

        return PaymentDraft(
            amount = amount,
            kind = kind,
            merchant = merchant,
            accountHint = accountNameFor(packageName),
            sourcePackage = packageName,
            rawText = content,
            timestamp = System.currentTimeMillis(),
            fingerprint = fingerprint(packageName, amount, merchant, kind)
        )
    }

    fun parseScreen(packageName: String, texts: List<String>): PaymentDraft? {
        val content = texts.filter { it.isNotBlank() }.joinToString(" ")
        if (content.isBlank()) return null
        if (noiseKeywords.any { content.contains(it) }) return null

        // 屏幕上必须真的出现「支付成功 / 付款成功」这类强信号，避免误触发
        val strongSignal = content.contains("支付成功") || content.contains("付款成功") ||
                content.contains("交易成功") || content.contains("收款到账") ||
                content.contains("已支付")
        if (!strongSignal) return null

        val amount = extractAmount(content) ?: return null
        if (amount <= 0) return null

        val isIncome = incomeKeywords.any { content.contains(it) } && !content.contains("支付成功")
        val merchant = extractMerchant(content) ?: "屏幕识别"

        return PaymentDraft(
            amount = amount,
            kind = if (isIncome) "INCOME" else "EXPENSE",
            merchant = merchant,
            accountHint = accountNameFor(packageName),
            sourcePackage = packageName,
            rawText = content.take(500),
            timestamp = System.currentTimeMillis(),
            fingerprint = fingerprint(packageName, amount, merchant, if (isIncome) "INCOME" else "EXPENSE"),
            confidence = 0.72f
        )
    }

    fun extractAmount(text: String): Long? {
        for (pattern in amountPatterns) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues.getOrNull(1)?.replace(",", "") ?: continue
            val value = raw.toDoubleOrNull() ?: continue
            if (value <= 0.0 || value > 10_000_000.0) continue
            return Math.round(value * 100)
        }
        return null
    }

    private fun extractMerchant(text: String): String? {
        for (pattern in merchantPatterns) {
            val match = pattern.find(text) ?: continue
            val value = match.groupValues.getOrNull(1)?.trim()
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun defaultMerchant(packageName: String, title: String): String = when (packageName) {
        "com.tencent.mm" -> if (title.isNotBlank()) title else "微信支付"
        "com.eg.android.AlipayGphone" -> "支付宝"
        else -> title.ifBlank { accountNameFor(packageName) }
    }

    fun accountNameFor(packageName: String): String = when (packageName) {
        "com.tencent.mm" -> "微信"
        "com.eg.android.AlipayGphone" -> "支付宝"
        "com.taobao.taobao" -> "支付宝"
        "com.jingdong.app.mall" -> "京东"
        "me.ele" -> "饿了么"
        "com.sankuai.meituan" -> "美团"
        "com.xunmeng.pinduoduo" -> "拼多多"
        else -> "其它"
    }

    private fun fingerprint(pkg: String, amount: Long, merchant: String, kind: String): String {
        // 同一分钟内、同来源、同金额、同商户视为同一次支付
        val bucket = System.currentTimeMillis() / 60_000
        val raw = "$pkg|$amount|$merchant|$kind|$bucket"
        val digest = MessageDigest.getInstance("SHA-1").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
    }
}
