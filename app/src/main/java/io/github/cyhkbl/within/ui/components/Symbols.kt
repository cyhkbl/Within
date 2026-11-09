package io.github.cyhkbl.within.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.R

/** Material Symbols 可变字体：图标直接用 ligature 名称渲染，避免打包上千个 vector。 */
val MaterialSymbols = FontFamily(Font(R.font.material_symbols_outlined))

@Composable
fun SymbolText(
    name: String,
    size: Dp = 22.dp,
    color: Color = LocalContentColor.current,
    modifier: Modifier = Modifier
) {
    Text(
        text = SymbolNames.resolve(name),
        fontFamily = MaterialSymbols,
        fontSize = size.value.sp,
        lineHeight = (size.value * 1.1f).sp,
        letterSpacing = 0.sp,
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}

/** 分类图标：彩色圆底 + 符号，Expressive 里这种「容器化小图标」很常见。 */
@Composable
fun CategoryGlyph(
    icon: String,
    size: Dp = 44.dp,
    selected: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    container: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        SymbolText(
            name = icon,
            size = size * 0.55f,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else tint
        )
    }
}
