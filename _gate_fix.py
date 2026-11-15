import io
p = r"app\src\main\java\io\github\cyhkbl\within\AutoRecordGate.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """        val started = try {
            context.startActivity(intent)
            true
        } catch (t: Throwable) {
            Log.w("AutoRecordGate", "无法直接拉起记账弹窗，降级为通知", t)
            false
        }
        if (!started) {
            Notifier.postDraft(context, draft)
        }"""
new = """        // Android 10 之后后台 startActivity 可能既不起作用也不抛异常，
        // 所以不依赖它的返回值，一律再补一条全屏意图通知兜底。
        try {
            context.startActivity(intent)
        } catch (t: Throwable) {
            Log.w("AutoRecordGate", "直接拉起记账弹窗失败，改为通知", t)
        }
        Notifier.postDraft(context, draft)"""
if old in s:
    s = s.replace(old, new, 1); print("gate OK")
else:
    print("gate MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
