import io

# 1) Manifest：前台服务 + 开机广播
p = r"app\src\main\AndroidManifest.xml"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace('''    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />''',
'''    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />''')

if "KeepAliveService" not in s:
    s = s.replace('''        <service
            android:name=".autolog.PaymentNotificationService"''',
'''        <service
            android:name=".autolog.KeepAliveService"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property
                android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                android:value="Keeps payment detection running so the ledger prompt can appear after a payment" />
        </service>

        <receiver
            android:name=".autolog.BootReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

        <service
            android:name=".autolog.PaymentNotificationService"''')
    print("manifest OK")

io.open(p, "w", encoding="utf-8", newline="").write(s)

# 2) LedgerApp：启动时拉起常驻服务
p = r"app\src\main\java\io\github\cyhkbl\within\LedgerApp.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
if "KeepAliveService.start" not in s:
    s = s.replace("        AutoRecordGate.attach(this)",
                  "        AutoRecordGate.attach(this)\n        KeepAliveService.start(this)")
    if "import io.github.cyhkbl.within.autolog.KeepAliveService" not in s:
        s = s.replace("import io.github.cyhkbl.within.data.LedgerRepository",
                      "import io.github.cyhkbl.within.autolog.KeepAliveService\nimport io.github.cyhkbl.within.data.LedgerRepository", 1)
    io.open(p, "w", encoding="utf-8", newline="").write(s)
    print("LedgerApp OK")
