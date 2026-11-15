package io.github.cyhkbl.within.autolog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.github.cyhkbl.within.AutoRecordGate
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.util.MoneyUtils

object Notifier {

    private const val CHANNEL_ID = "auto_record"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动记账",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "检测到支付完成时立刻提示记账"
                setShowBadge(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 发一条「全屏意图」通知。
     *
     * Android 10 之后后台直接 startActivity 会被系统静默拦掉，光靠拉起 Activity
     * 是不行的；带 fullScreenIntent 的高优先级通知是系统允许的例外路径，
     * 能在用户正看着支付页时把记账界面推上来。
     */
    fun postDraft(context: Context, draft: PaymentDraft) {
        ensureChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val intent = Intent(context, AutoRecordActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AutoRecordGate.EXTRA_FINGERPRINT, draft.fingerprint)
        }
        val pending = PendingIntent.getActivity(
            context,
            draft.fingerprint.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${draft.accountHint} ${MoneyUtils.format(draft.amount)}")
            .setContentText("${draft.merchant} · 点一下确认记账")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setFullScreenIntent(pending, true)
            .build()

        try {
            manager.notify(draft.fingerprint.hashCode(), notification)
        } catch (t: Throwable) {
            // 通知被系统禁用时静默失败，不影响已经落库的草稿
        }
    }
}
