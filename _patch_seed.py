import io

# 种子账户用渠道
p = r"app\src\main\java\io\github\cyhkbl\within\data\DefaultData.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
pairs = [
    ('Triple("微信", "WALLET", "chat")', 'Triple("微信", "wechat", "chat")'),
    ('Triple("支付宝", "WALLET", "account_balance_wallet")', 'Triple("支付宝", "alipay", "account_balance_wallet")'),
    ('Triple("银行卡", "BANK", "account_balance")', 'Triple("银行卡", "other", "account_balance")'),
]
for old, new in pairs:
    if old in s:
        s = s.replace(old, new, 1); print("seed:", new.split(",")[0])
io.open(p, "w", encoding="utf-8", newline="").write(s)

# 导入时新建账户：按名字推断渠道
p = r"app\src\main\java\io\github\cyhkbl\within\io\ImportRunner.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = 'val id = repo.saveAccount(AccountEntity(name = name, type = "WALLET", sortOrder = 90))'
new = 'val id = repo.saveAccount(\n            AccountEntity(\n                name = name,\n                type = io.github.cyhkbl.within.data.AccountChannels.inferFromName(name, null),\n                sortOrder = 90\n            )\n        )'
if old in s:
    s = s.replace(old, new, 1); print("import channel OK")
else:
    print("import MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
