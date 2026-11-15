import io

p = r"app\src\main\java\io\github\cyhkbl\within\onboarding\AutoRecordPermissions.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""    private inline fun safeStart(
        context: Context,
        intent: Intent,
        fallback: (() -> Unit)? = null
    ): Boolean {""",
"""    private fun safeStart(
        context: Context,
        intent: Intent,
        fallback: (() -> Unit)? = null
    ): Boolean {""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("safeStart fixed")

p = r"app\src\main\java\io\github\cyhkbl\within\onboarding\OnboardingScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
s = s.replace("""            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.full.let { MaterialTheme.shapes.large },
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {""",
"""            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {""")
s = s.replace("""    Surface(shape = MaterialTheme.shapes.full, color = container) {""",
"""    Surface(shape = CircleShape, color = container) {""")
io.open(p, "w", encoding="utf-8", newline="").write(s)
print("shapes fixed")
