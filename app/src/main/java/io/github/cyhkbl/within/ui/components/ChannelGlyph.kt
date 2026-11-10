package io.github.cyhkbl.within.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.cyhkbl.within.R

private data class ChannelSkin(val iconRes: Int, val brand: Color)

/**
 * 账户渠道图标：品牌色做低透明容器，图标本身用品牌色，
 * 和 Material 3 Expressive 的 tonal icon 一个路子——既有品牌识别度，又不打架。
 */
@Composable
fun ChannelGlyph(channel: String?, size: Dp = 44.dp) {
    val skin = channelSkin(channel)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(skin.brand.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(skin.iconRes),
            contentDescription = null,
            tint = skin.brand,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}

private fun channelSkin(channel: String?): ChannelSkin = when (channel) {
    "wechat" -> ChannelSkin(R.drawable.ic_channel_wechat, Color(0xFF07C160))
    "alipay" -> ChannelSkin(R.drawable.ic_channel_alipay, Color(0xFF1677FF))
    "icbc" -> ChannelSkin(R.drawable.ic_channel_icbc, Color(0xFFC7000B))
    "boc" -> ChannelSkin(R.drawable.ic_channel_boc, Color(0xFFA6093D))
    "cmb" -> ChannelSkin(R.drawable.ic_channel_cmb, Color(0xFFD3001D))
    "ccb" -> ChannelSkin(R.drawable.ic_channel_ccb, Color(0xFF0055A5))
    "abc" -> ChannelSkin(R.drawable.ic_channel_abc, Color(0xFF009B4C))
    "bocom" -> ChannelSkin(R.drawable.ic_channel_bocom, Color(0xFF005BAC))
    "cash" -> ChannelSkin(R.drawable.ic_channel_cash, Color(0xFF00875F))
    "credit" -> ChannelSkin(R.drawable.ic_channel_credit, Color(0xFF7A4FCF))
    else -> ChannelSkin(R.drawable.ic_channel_other, Color(0xFF5C6470))
}
