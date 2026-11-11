import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """                listOf(
                    AccountChannels.FUND to AccountChannels.fundChannels,
                    AccountChannels.CREDIT to AccountChannels.creditChannels,
                    AccountChannels.RECHARGE to AccountChannels.rechargeChannels
                ).forEach { (group, channels) ->"""
new = """                listOf(
                    AccountChannels.FUND to AccountChannels.fundChannels,
                    AccountChannels.CREDIT to AccountChannels.creditChannels,
                    AccountChannels.RECHARGE to AccountChannels.rechargeChannels,
                    AccountChannels.INVEST to AccountChannels.investChannels,
                    AccountChannels.DEBT to AccountChannels.debtChannels
                ).forEach { (group, channels) ->"""
if old in s:
    s = s.replace(old, new, 1); print("groups OK")
else:
    print("groups MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
