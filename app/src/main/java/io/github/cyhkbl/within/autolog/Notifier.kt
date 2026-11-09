package io.github.cyhkbl.within.autolog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.github.cyhkbl.within.AutoRecordGate
import io.github.cyhkbl.within.MainActivity
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
                description = "检测到支付完成时提示记账"
            }
            manager.createNotificationChannel(channel)
        }
    }

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
            .setContentText("${draft.merchant} · 点击确认记账")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        manager.notify(draft.fingerprint.hashCode(), notification)
    }
}
