package io.github.cyhkbl.within.autolog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.onboarding.AutoRecordPermissions

/**
 * 自动记账的健康自检。
 *
 * ColorOS 这类系统会把无障碍服务「解绑但保留配置」——设置里看着是开的，
 * 实际系统根本没连上，表现就是支付后什么都不弹，用户也查不出原因。
 * 每次启动和回到前台时查一次，断了就发通知让用户重开。
 */
object AutoRecordHealth {

    private const val CHANNEL_ID = "auto_record_health"
    private const val NOTIF_ID = 2001

    data class Status(
        val accessibilityConfigured: Boolean,
        val accessibilityBound: Boolean,
        val listenerConfigured: Boolean,
        val listenerLive: Boolean
    ) {
        val accessibilityBroken: Boolean get() = accessibilityConfigured && !accessibilityBound
        val listenerBroken: Boolean get() = listenerConfigured && !listenerLive
        val hasProblem: Boolean get() = accessibilityBroken || listenerBroken
    }

    fun inspect(context: Context): Status {
        val a11yConfigured = AutoRecordPermissions.accessibilityEnabled(context)
        // 绑定状态只能从系统服务里查；这里用更严格的口径：配置里有，但拿不到事件
        val a11yBound = a11yConfigured && isAccessibilityBound(context)
        val listenerConfigured = AutoRecordPermissions.notificationListenerEnabled(context)
        val listenerLive = listenerConfigured && isListenerLive(context)
        return Status(a11yConfigured, a11yBound, listenerConfigured, listenerLive)
    }

    private fun isAccessibilityBound(context: Context): Boolean {
        val manager = context.getSystemService(android.view.accessibility.AccessibilityManager::class.java)
            ?: return false
        val running = manager.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return running.any { it.resolveInfo?.serviceInfo?.packageName == context.packageName }
    }

    private fun isListenerLive(context: Context): Boolean {
        // 没有公开的「是否已连接」查询接口，只能读配置状态；
        // 真正的连接情况要靠实测支付才能确认
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabled.contains(context.packageName)
    }

    /** 有问题时发一条可点击的通知，引导用户去重开。 */
    fun notifyIfBroken(context: Context) {
        val status = inspect(context)
        if (!status.hasProblem) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "自动记账状态",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "自动记账失效时提醒你重新授权" }
            )
        }

        val missing = when {
            status.accessibilityBroken && status.listenerBroken -> "无障碍和通知监听都被系统断开了"
            status.accessibilityBroken -> "无障碍服务被系统断开了"
            else -> "通知监听被系统断开了"
        }

        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        manager.notify(
            NOTIF_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("自动记账已失效")
                .setContentText("$missing，点这里重新开启")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
        )
    }
}
