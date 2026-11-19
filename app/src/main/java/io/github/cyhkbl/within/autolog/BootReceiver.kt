package io.github.cyhkbl.within.autolog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 开机后把常驻服务拉起来，否则重启一次自动记账就彻底停了。 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                KeepAliveService.start(context)
            }
        }
    }
}
