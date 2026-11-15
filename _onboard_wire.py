import io

# 1) MainActivity：首启进引导
p = r"app\src\main\java\io\github\cyhkbl\within\MainActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue""",
"""import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue""")
s = s.replace("""import io.github.cyhkbl.within.ui.LedgerRoot""",
"""import io.github.cyhkbl.within.onboarding.OnboardingPrefs
import io.github.cyhkbl.within.onboarding.OnboardingScreen
import io.github.cyhkbl.within.ui.LedgerRoot""")
s = s.replace("""                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LedgerRoot(repo)
                }""",
"""                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var onboarded by remember { mutableStateOf(OnboardingPrefs.isDone(this@MainActivity)) }
                    if (onboarded) {
                        LedgerRoot(repo)
                    } else {
                        OnboardingScreen(onFinish = { onboarded = true })
                    }
                }""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("MainActivity OK")

# 2) Manifest：预见性返回 + 全屏通知 + 电池优化权限
p = r"app\src\main\AndroidManifest.xml"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />""",
"""    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />""")
s = s.replace("""    <application
        android:name=".LedgerApp\"""",
"""    <application
        android:name=".LedgerApp"
        android:enableOnBackInvokedCallback="true\"""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("Manifest OK")
