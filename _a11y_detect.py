import io
p = r"app\src\main\java\io\github\cyhkbl\within\onboarding\AutoRecordPermissions.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""    /** 无障碍服务是否已启用。 */
    fun accessibilityEnabled(context: Context): Boolean {
        val expected = ComponentName(context, PaymentAccessibilityService::class.java)""",
"""    /** 无障碍服务是否已启用。 */
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
        val expected = ComponentName(context, PaymentAccessibilityService::class.java)""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("detection OK")
