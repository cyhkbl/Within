import io

# Repository：把老账户类型迁到渠道
p = r"app\src\main\java\io\github\cyhkbl\within\data\LedgerRepository.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "migrateAccountChannels" not in s:
    anchor = "    /**\n     * 把某个账户的余额校准到目标值"
    add = """    /**
     * 老数据的账户类型是 CASH/WALLET/BANK 这类粗分类，这里按账户名一次性迁到
     * 具体渠道（微信、支付宝、工商银行……），只跑一次。
     */
    suspend fun migrateAccountChannels(): Int {
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
    }

"""
    s = s.replace(anchor, add + anchor, 1)
    if "import io.github.cyhkbl.within.data.AccountChannels" not in s:
        s = s.replace("import io.github.cyhkbl.within.data.db.AccountEntity",
                      "import io.github.cyhkbl.within.data.AccountChannels\nimport io.github.cyhkbl.within.data.db.AccountEntity", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("repo migration added")

# LedgerApp：启动时跑一次
p = r"app\src\main\java\io\github\cyhkbl\within\LedgerApp.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = "            repo.normalizeRefundFlags()"
new = "            repo.normalizeRefundFlags()\n            // 账户类型从「钱包/银行卡」迁到「微信/支付宝/工商银行」这类渠道\n            repo.migrateAccountChannels()"
if "migrateAccountChannels" not in s:
    s = s.replace(old, new, 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("app migration wired")
