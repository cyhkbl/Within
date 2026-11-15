package io.github.cyhkbl.within

import android.app.Application
import android.content.Intent
import android.util.Log
import io.github.cyhkbl.within.autolog.AutoRecordActivity
import io.github.cyhkbl.within.autolog.Notifier
import io.github.cyhkbl.within.autolog.PaymentDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 自动记账的入口闸门：去重、暂存草稿、拉起确认弹窗。
 * 两个入口（通知监听 / 无障碍）都走这里，避免同一次支付弹两次。
 */
object AutoRecordGate {

    const val EXTRA_FINGERPRINT = "auto_record_fingerprint"

    private var app: Application? = null
    private val recentFingerprints = LinkedHashSet<String>()

    private val _draft = MutableStateFlow<PaymentDraft?>(null)
    val draft: StateFlow<PaymentDraft?> = _draft

    var autoEnabled: Boolean = true

    fun attach(application: Application) {
        app = application
    }

    @Synchronized
    fun offer(incoming: PaymentDraft) {
        if (!autoEnabled) return
        if (!recentFingerprints.add(incoming.fingerprint)) return
        if (recentFingerprints.size > 30) {
            val oldest = recentFingerprints.first()
            recentFingerprints.remove(oldest)
        }
        _draft.value = incoming
        showPrompt(incoming)
    }

    fun clear() {
        _draft.value = null
    }

    private fun showPrompt(draft: PaymentDraft) {
        val context = app ?: return
        val intent = Intent(context, AutoRecordActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(EXTRA_FINGERPRINT, draft.fingerprint)
        }
        // Android 10 之后后台 startActivity 可能既不起作用也不抛异常，
        // 所以不依赖它的返回值，一律再补一条全屏意图通知兜底。
        try {
            context.startActivity(intent)
        } catch (t: Throwable) {
            Log.w("AutoRecordGate", "直接拉起记账弹窗失败，改为通知", t)
        }
        Notifier.postDraft(context, draft)
    }
}
