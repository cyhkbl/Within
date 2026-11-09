package io.github.cyhkbl.within.autolog

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.cyhkbl.within.AutoRecordGate

/**
 * 兜底方式：通知被折叠/关闭时，直接在支付结果页上读金额。
 * 只在白名单应用里、出现「支付成功」这类强信号时才触发。
 */
class PaymentAccessibilityService : AccessibilityService() {

    private var lastHandledAt = 0L
    private var lastFingerprint = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        val pkg = e.packageName?.toString() ?: return
        if (pkg !in PaymentNotificationService.WATCHED) return
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            e.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        val now = System.currentTimeMillis()
        if (now - lastHandledAt < 1200L) return

        val root = rootInActiveWindow ?: return
        val texts = ArrayList<String>(32)
        collectTexts(root, texts, 0)
        if (texts.isEmpty()) return

        val draft = PaymentParser.parseScreen(pkg, texts) ?: return
        if (draft.fingerprint == lastFingerprint && now - lastHandledAt < 20_000L) return

        lastFingerprint = draft.fingerprint
        lastHandledAt = now
        AutoRecordGate.offer(draft)
    }

    private fun collectTexts(node: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
        if (node == null || out.size >= 80 || depth > 12) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        for (i in 0 until node.childCount) {
            collectTexts(node.getChild(i), out, depth + 1)
        }
    }

    override fun onInterrupt() = Unit
}
