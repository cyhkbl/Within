import io

# 1) 简化 listener 判断，去掉不存在的 requestRebind
p = r"app\src\main\java\io\github\cyhkbl\within\autolog\AutoRecordHealth.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace('''    private fun isListenerLive(context: Context): Boolean {
        // NotificationListenerService.requestRebind 会触发系统重新连一次；
        // 能连上就说明配置有效，连不上通常是被系统压制了
        return try {
            PaymentNotificationService.requestRebind(context)
            true
        } catch (t: Throwable) {
            false
        }
    }''',
'''    private fun isListenerLive(context: Context): Boolean {
        // 没有公开的「是否已连接」查询接口，只能读配置状态；
        // 真正的连接情况要靠实测支付才能确认
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabled.contains(context.packageName)
    }''')
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("health fixed")

# 2) LedgerApp 启动时自检
p = r"app\src\main\java\io\github\cyhkbl\within\LedgerApp.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "AutoRecordHealth" not in s:
    s = s.replace("        KeepAliveService.start(this)",
                  "        KeepAliveService.start(this)\n        AutoRecordHealth.notifyIfBroken(this)")
    if "import io.github.cyhkbl.within.autolog.AutoRecordHealth" not in s:
        s = s.replace("import io.github.cyhkbl.within.autolog.KeepAliveService",
                      "import io.github.cyhkbl.within.autolog.AutoRecordHealth\nimport io.github.cyhkbl.within.autolog.KeepAliveService", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("LedgerApp health OK")
