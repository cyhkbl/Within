package io.github.cyhkbl.within.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fallback 色板：青绿主色，适合财务类产品；Android 12+ 上会被动态取色覆盖。
private val TealPrimary = Color(0xFF00696D)
private val TealOnPrimary = Color(0xFFFFFFFF)
private val TealPrimaryContainer = Color(0xFF6FF6FC)
private val TealOnPrimaryContainer = Color(0xFF002022)

private val SandSecondary = Color(0xFF4A6363)
private val SandSecondaryContainer = Color(0xFFCCE8E7)
private val SandOnSecondaryContainer = Color(0xFF051F1F)

private val CoralTertiary = Color(0xFF9C4146)
private val CoralTertiaryContainer = Color(0xFFFFDADA)
private val CoralOnTertiaryContainer = Color(0xFF410005)

private val LightNeutralSurface = Color(0xFFF5FAFA)
private val LightSurfaceVariant = Color(0xFFDAE4E4)
private val OnSurfaceLight = Color(0xFF191C1C)

val ExpressiveLightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = SandSecondary,
    secondaryContainer = SandSecondaryContainer,
    onSecondaryContainer = SandOnSecondaryContainer,
    tertiary = CoralTertiary,
    tertiaryContainer = CoralTertiaryContainer,
    onTertiaryContainer = CoralOnTertiaryContainer,
    background = LightNeutralSurface,
    onBackground = OnSurfaceLight,
    surface = LightNeutralSurface,
    onSurface = OnSurfaceLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF3F4949)
)

private val DarkPrimary = Color(0xFF4CDADC)
private val DarkOnPrimary = Color(0xFF003738)
private val DarkPrimaryContainer = Color(0xFF004F51)
private val DarkOnPrimaryContainer = Color(0xFF6FF6FC)
private val DarkSurface = Color(0xFF191C1C)
private val DarkSurfaceVariant = Color(0xFF3F4949)
private val DarkOnSurface = Color(0xFFE0E3E2)

val ExpressiveDarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Color(0xFFB0CCCB),
    secondaryContainer = Color(0xFF324B4B),
    onSecondaryContainer = Color(0xFFCCE8E7),
    tertiary = Color(0xFFFFB3B4),
    tertiaryContainer = Color(0xFF7C292F),
    onTertiaryContainer = Color(0xFFFFDADA),
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBEC8C8)
)

// 财务语义色：支出用红、收入用绿，不跟动态主色走，保证一眼能分辨。
val ExpenseRed = Color(0xFFD93F35)
val IncomeGreen = Color(0xFF1F9D55)
val TransferBlue = Color(0xFF3B6FE0)
