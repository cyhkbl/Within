package io.github.cyhkbl.within.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import io.github.cyhkbl.within.autolog.PaymentAccessibilityService
import io.github.cyhkbl.within.autolog.PaymentNotificationService

/**
 * 自动记账要跑起来，需要一串系统授权。这里集中检测和跳转，
 * 免得用户不知道哪里没开——之前弹窗一直不出现就是因为无障碍和通知监听都没授。
 */
object AutoRecordPermissions {

    /** 无障碍服务是否已启用。 */
    fun accessibilityEnabled(context: Context): Boolean {
        // 优先问系统当前真正绑定着哪些服务；adb 直写 settings 会被系统回滚，
        // 只有真正跑起来的服务才作数
        val manager = context.getSystemService(android.view.accessibility.AccessibilityManager::class.java)
        if (manager != null && manager.isEnabled) {
            val running = manager.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            if (running.any { it.resolveInfo?.serviceInfo?.packageName == context.packageName }) {
                return true
            }
        }
        val expected = ComponentName(context, PaymentAccessibilityService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            val part = splitter.next()
            val component = ComponentName.unflattenFromString(part) ?: continue
            if (component == expected) return true
        }
        return false
    }

    /** 通知使用权是否已授予。 */
    fun notificationListenerEnabled(context: Context): Boolean {
        val expected = ComponentName(context, PaymentNotificationService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    /** 是否已加入电池优化白名单（不被省电策略掐掉）。 */
    fun batteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** 悬浮窗权限。Android 10 之后后台起 Activity 会被拦，悬浮窗是可靠兜底。 */
    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    /** 通知权限（Android 13+ 需要运行时申请）。 */
    fun notificationsGranted(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * 自启动没有公开的查询接口，没法真的检测。
     * 退一步：能读到我们的电池白名单就认为用户已经在系统里动过手了。
     */
    fun autoStartLikelyAllowed(context: Context): Boolean =
        batteryOptimizationIgnored(context)

    /** 四项是否都齐了。 */
    fun allGranted(context: Context): Boolean =
        accessibilityEnabled(context) &&
            notificationListenerEnabled(context) &&
            batteryOptimizationIgnored(context) &&
            notificationsGranted(context)

    // ── 跳转到对应的系统设置页 ──

    fun openAccessibilitySettings(context: Context) {
        safeStart(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openNotificationListenerSettings(context: Context) {
        safeStart(
            context,
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        )
    }

    @Suppress("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        safeStart(context, intent) {
            safeStart(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        safeStart(context, intent) {
            safeStart(context, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }

    fun openAppNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        safeStart(context, intent) {
            safeStart(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
        }
    }

    /** 厂商的自启动管理页，ColorOS / OxygenOS 上是这个。 */
    fun openAutoStartSettings(context: Context) {
        val candidates = listOf(
            ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity"),
            ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
        )
        for (component in candidates) {
            val intent = Intent().apply {
                this.component = component
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (context.packageManager.resolveActivity(intent, 0) != null) {
                if (safeStart(context, intent, null)) return
            }
        }
        safeStart(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
    }

    private fun safeStart(
        context: Context,
        intent: Intent,
        fallback: (() -> Unit)? = null
    ): Boolean {
        val withFlag = Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(withFlag)
            true
        } catch (t: Throwable) {
            if (fallback != null) {
                fallback()
                true
            } else {
                false
            }
        }
    }
}

/** 引导是否已经走完，用 SharedPreferences 记一下。 */
object OnboardingPrefs {
    private const val FILE = "within_onboarding"
    private const val KEY_DONE = "done"

    fun isDone(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_DONE, false)

    fun markDone(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DONE, true).apply()
    }
}
