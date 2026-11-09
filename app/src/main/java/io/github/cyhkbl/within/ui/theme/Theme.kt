package io.github.cyhkbl.within.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 官方 Material 3 Expressive 主题。
 * 依赖 material3 1.5.0-rc01（第一条公开 Expressive API 的版本线），
 * 由此要求 Compose 1.12 / AGP 9.1+ / compileSdk 37。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> ExpressiveDarkColors
        else -> ExpressiveLightColors
    }

    MaterialExpressiveTheme(
        colorScheme = colors,
        typography = LedgerTypography,
        shapes = LedgerShapes,
        content = content
    )
}
