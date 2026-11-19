package io.github.cyhkbl.within.autolog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.cyhkbl.within.MainActivity
import io.github.cyhkbl.within.R

/**
 * 常驻前台服务。
 *
 * 无障碍和通知监听这两个服务由系统绑定，本身没法互相保活；
 * ColorOS 这类系统空闲几天后会把它们解绑且不再重新拉起，表现就是
 * 「一开始能自动记账，过几天就悄无声息了」。
 *
 * 挂一个前台服务把进程优先级顶到前台，系统清理时会跳过我们。
 */
class KeepAliveService : Service() {

    companion object {
        private const val CHANNEL_ID = "keep_alive"
        private const val NOTIF_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, KeepAliveService::class.java)
            try {
                context.startForegroundService(intent)
            } catch (t: Throwable) {
                // Android 12+ 在后台启动前台服务会抛 ForegroundServiceStartNotAllowed，
                // 这种情况交给下次前台启动时再拉起
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 被杀后让系统再拉起一次
        return START_STICKY
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动记账常驻",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "保持应用在后台，支付时才能及时弹出记账卡片"
                setShowBadge(false)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("自动记账已开启")
            .setContentText("支付完成后会自动弹出记账卡片")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pending)
            .build()
    }
}
