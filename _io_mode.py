import io

# 1) 转场：加横向滑动 + 更长的缓动，避免「没动画」的观感
p = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""        // 底部四个 tab 是平级的，来回切换用同一套淡入淡出，不区分前进/返回
        enterTransition = { fadeIn(tween(180)) },
        exitTransition = { fadeOut(tween(140)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition = { fadeOut(tween(140)) }""",
"""        // 底部四个 tab 平级，来回切换同一套「轻微横移 + 淡入」，
        // 纯淡入淡出时长太短会让人以为没有动画
        enterTransition = {
            fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 16 }
        },
        exitTransition = {
            fadeOut(tween(180, easing = FastOutLinearInEasing)) +
                slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it / 20 }
        },
        popEnterTransition = {
            fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 16 }
        },
        popExitTransition = {
            fadeOut(tween(180, easing = FastOutLinearInEasing)) +
                slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 20 }
        }""")
for imp in ["import androidx.compose.animation.slideInHorizontally",
            "import androidx.compose.animation.slideOutHorizontally",
            "import androidx.compose.animation.core.FastOutSlowInEasing",
            "import androidx.compose.animation.core.FastOutLinearInEasing"]:
    if imp not in s:
        s = s.replace("import androidx.compose.animation.core.tween",
                      imp + "\nimport androidx.compose.animation.core.tween", 1)
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("transitions OK")

# 2) ImportExportActivity 支持只显示导入或只显示导出
p = r"app\src\main\java\io\github\cyhkbl\within\ui\io\ImportExportActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

s = s.replace("""class ImportExportActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ImportExportScreen(context = this, repo = repo, onBack = { finish() })
                }
            }
        }
    }
}""",
"""class ImportExportActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "io_mode"
        const val MODE_IMPORT = "import"
        const val MODE_EXPORT = "export"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_IMPORT
        setContent {
            LedgerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ImportExportScreen(
                        context = this,
                        repo = repo,
                        mode = mode,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}""")

s = s.replace("""private fun ImportExportScreen(
    context: Context,
    repo: LedgerRepository,
    onBack: () -> Unit
) {""",
"""private fun ImportExportScreen(
    context: Context,
    repo: LedgerRepository,
    mode: String,
    onBack: () -> Unit
) {
    val showImport = mode == ImportExportActivity.MODE_IMPORT
    val showExport = mode == ImportExportActivity.MODE_EXPORT""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("activity mode OK")
