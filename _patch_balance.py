import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\calendar\CalendarScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = "        txs.groupBy { DateUtils.toLocalDate(it.timestamp) }"
new = "        txs.filter { it.source != \"ADJUST\" }.groupBy { DateUtils.toLocalDate(it.timestamp) }"
if old in s:
    s = s.replace(old, new, 1); print("OK calendar filter")
else:
    print("MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)

# 账户管理：编辑余额时生成平账记录，而不是直接改字段
p = r"app\src\main\java\io\github\cyhkbl\within\ui\accounts\AccountsManageActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """                    onSave = { entity ->
                        onDismiss()
                        scope.launch { repo.saveAccount(entity) }
                    }"""
new = """                    onSave = { entity ->
                        onDismiss()
                        scope.launch {
                            if (entity.id == 0L) {
                                // 新建账户：余额就是期初值，直接写进去
                                repo.saveAccount(entity)
                            } else {
                                val old = repo.accountById(entity.id)
                                // 余额不许直接改字段，否则流水和余额会对不上；
                                // 先保存其它字段，再把余额差额记成一笔平账。
                                repo.saveAccount(entity.copy(balance = old?.balance ?: 0L))
                                if (old != null && old.balance != entity.balance) {
                                    repo.adjustAccountBalance(entity.id, entity.balance)
                                }
                            }
                        }
                    }"""
if old in s:
    s = s.replace(old, new, 1); print("OK account save")
else:
    print("MISS account save")
io.open(p, "w", encoding="utf-8", newline="").write(s)
