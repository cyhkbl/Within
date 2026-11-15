import io
p = r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

old = """    suspend fun migrateAccountChannels(): Int {
        val accounts = db.accountDao().listAll()
        var changed = 0
        accounts.forEach { acc ->
            if (AccountChannels.byKey(acc.type) == null) {
                val channel = AccountChannels.inferFromName(acc.name, acc.type)
                db.accountDao().update(acc.copy(type = channel))
                changed++
            }
        }
        return changed
    }"""

new = """    suspend fun migrateAccountChannels(): Int {
        val accounts = db.accountDao().listAll()
        var changed = 0
        accounts.forEach { acc ->
            val known = AccountChannels.byKey(acc.type)
            val inferred = AccountChannels.inferFromName(acc.name, acc.type)
            // 三种情况要改：
            // 1. 老数据是 CASH/WALLET 这类已废弃的类型
            // 2. 停在通用「银行卡」，但账户名能认出是哪家银行
            // 3. 停在「其它」，但账户名能认出具体渠道
            val upgradeable = known == null ||
                (acc.type == "bank" && inferred != "bank") ||
                (acc.type == "other" && inferred != "other")
            if (upgradeable && inferred != acc.type) {
                db.accountDao().update(acc.copy(type = inferred))
                changed++
            }
        }
        return changed
    }"""

if old in s:
    s = s.replace(old, new, 1); print("migration OK")
else:
    print("migration MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
