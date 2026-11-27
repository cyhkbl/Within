package io.github.cyhkbl.within.autolog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 收款方向的回归测试。
 *
 * 「张三向你转账 1.00 元」这类通知既不含「转入」也不含支付措辞，
 * 早先会被直接丢掉，表现就是收款时什么都不弹。
 */
class PaymentParserTest {

    private fun parse(pkg: String, title: String, text: String) =
        PaymentParser.parseNotification(pkg, title, text)

    @Test
    fun `wechat collection is income`() {
        val d = parse("com.tencent.mm", "微信支付", "微信支付收款1.00元")
        assertNotNull("收款通知应该有结果", d)
        assertEquals("INCOME", d!!.kind)
        assertEquals(100L, d.amount)
    }

    @Test
    fun `transfer from someone is income`() {
        val d = parse("com.tencent.mm", "张三", "张三向你转账 5.00 元")
        assertNotNull("向你转账应该识别为收款", d)
        assertEquals("INCOME", d!!.kind)
        assertEquals(500L, d.amount)
    }

    @Test
    fun `transfer to someone is expense`() {
        val d = parse("com.tencent.mm", "微信支付", "你向李四转账 5.00 元")
        assertNotNull(d)
        assertEquals("EXPENSE", d!!.kind)
        assertEquals(500L, d.amount)
    }

    @Test
    fun `alipay arrival is income`() {
        val d = parse("com.eg.android.AlipayGphone", "支付宝", "支付宝到账 2.00 元")
        assertNotNull(d)
        assertEquals("INCOME", d!!.kind)
        assertEquals(200L, d.amount)
    }

    @Test
    fun `payment success is expense`() {
        val d = parse("com.tencent.mm", "微信支付", "微信支付凭证：支付成功 ¥28.80")
        assertNotNull(d)
        assertEquals("EXPENSE", d!!.kind)
        assertEquals(2880L, d.amount)
    }

    @Test
    fun `failed payment is ignored`() {
        assertNull(parse("com.tencent.mm", "微信支付", "支付失败 ¥28.80"))
    }

    @Test
    fun `notification without amount is ignored`() {
        assertNull(parse("com.tencent.mm", "微信支付", "你有一笔新的收款"))
    }
}
