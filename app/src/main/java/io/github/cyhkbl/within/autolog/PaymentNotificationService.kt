package io.github.cyhkbl.within.autolog

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.github.cyhkbl.within.AutoRecordGate

/** 监听微信/支付宝等的支付通知。是最省电、最稳的检测方式。 */
class PaymentNotificationService : NotificationListenerService() {

    companion object {
        val WATCHED = setOf(
            "com.tencent.mm",
            "com.eg.android.AlipayGphone",
            "com.taobao.taobao",
            "com.jingdong.app.mall",
            "me.ele",
            "com.sankuai.meituan",
            "com.xunmeng.pinduoduo"
        )
    }

    override fun onCreate() {
        super.onCreate()
        Notifier.ensureChannel(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val pkg = sbn.packageName ?: return
        if (pkg !in WATCHED) return

        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val summary = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()
        val sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()

        val draft = PaymentParser.parseNotification(pkg, title, "$text $big $summary $sub") ?: return
        AutoRecordGate.offer(draft)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Notifier.ensureChannel(this)
    }
}
