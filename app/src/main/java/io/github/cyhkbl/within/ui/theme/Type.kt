package io.github.cyhkbl.within.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * M3 Expressive 的类型策略：默认字体族 + 更强调的数字层级。
 * 金额是记账 App 的主角，所以单独给一套等宽感更强的大字号样式。
 */
val LedgerTypography = Typography()

val AmountDisplay = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    lineHeight = 46.sp,
    letterSpacing = (-0.5).sp
)

val AmountMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 26.sp
)

val AmountSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp
)
