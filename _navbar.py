import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

old = """    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0)
            ) {
                destinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = { onNavigate(dest.route) },
                        icon = {
                            SymbolText(
                                name = dest.icon,
                                size = 24.dp,
                                color = if (currentRoute == dest.route)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(stringResource(dest.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { padding -> content(padding) }"""

new = """    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0),
                modifier = Modifier.height(82.dp)
            ) {
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onNavigate(dest.route) },
                        icon = {
                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1f else 0.92f,
                                animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
                                label = "navIconScale"
                            )
                            SymbolText(
                                name = dest.icon,
                                size = (26 * scale).dp,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                stringResource(dest.label),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold
                                else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding -> content(padding) }"""

if old in s:
    s = s.replace(old, new, 1)
    print("navbar OK")
else:
    print("navbar MISS")

for imp, line in [
    ("import androidx.compose.animation.core.animateFloatAsState", None),
    ("import androidx.compose.animation.core.spring", None),
    ("import androidx.compose.runtime.getValue", None),
    ("import androidx.compose.foundation.layout.height", None),
]:
    if imp not in s:
        s = s.replace("import androidx.compose.material3.MaterialTheme",
                      imp + "\nimport androidx.compose.material3.MaterialTheme", 1)

io.open(p, "w", encoding="utf-8", newline="").write(s)
