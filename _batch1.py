import io, re

def patch(path, pairs, must=True):
    s = io.open(path, encoding="utf-8", newline="").read().replace("\r\n", "\n")
    for old, new in pairs:
        if old in s:
            s = s.replace(old, new, 1)
        elif must:
            print("MISS in", path.split("\\")[-1], "->", old[:50])
    io.open(path, "w", encoding="utf-8", newline="").write(s)

# 1) 应用内改余额 -> 记为「平账」，计入收支
patch(r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt", [
("""    /**
     * 把某个账户的余额校准到目标值，差额记一条「余额调整」。
     *
     * 钱迹里手动改过余额会留下调整记录，但导出的账单文件不含这些记录，
     * 所以导入之后账户余额会和钱迹对不上。这里让用户按钱迹的数字校准一次，
     * 调整记录不计入收支统计，只影响账户余额。
     */""",
"""    /**
     * 把某个账户的余额校准到目标值，差额记一条「平账」。
     *
     * 应用内直接改余额是用户主动的对账动作，所以这笔差额算真实收支，
     * 进账单、进统计。从钱迹导入时若需要对齐余额，走的是另一条不计收支的路径。
     */"""),
("""                note = "余额调整",
                timestamp = System.currentTimeMillis(),
                excludeFromStats = true,
                source = "ADJUST\"""",
"""                note = "平账",
                timestamp = System.currentTimeMillis(),
                excludeFromStats = false,
                source = "ADJUST\""""),
])

# 2) 退款不进统计
patch(r"app\src\main\java\io\github\cyhkbl\within\ui\stats\StatsScreen.kt", [
("""    val txs = rawTxs.filter { it.source != "ADJUST" }""",
"""    // 退款单独处理，不进饼图和统计列表
    val txs = rawTxs.filter { it.source != "ADJUST" && it.refundedFromId == null }"""),
])

# 3) 资产页去掉小字
patch(r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt", [
("""                    Text(
                        "共 ${accounts.size} 个账户 · 点一下就能改余额",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
""", ""),
])

print("batch 1 done")
