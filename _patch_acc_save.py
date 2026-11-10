import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\accounts\AccountsManageActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """            onSave = { entity ->
                showDialog = false
                scope.launch { repo.saveAccount(entity) }
            }"""
new = """            onSave = { entity ->
                showDialog = false
                scope.launch {
                    if (entity.id == 0L) {
                        // 新建账户：这个余额就是期初值，直接写进去
                        repo.saveAccount(entity)
                    } else {
                        val old = repo.accountById(entity.id)
                        // 余额不能直接改字段，否则流水和余额对不上；
                        // 先保存其它字段，把余额差额记成一笔平账（不计入收支）
                        repo.saveAccount(entity.copy(balance = old?.balance ?: 0L))
                        if (old != null && old.balance != entity.balance) {
                            repo.adjustAccountBalance(entity.id, entity.balance)
                        }
                    }
                }
            }"""
if old in s:
    s = s.replace(old, new, 1); print("OK")
else:
    print("MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
