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

private data class ChannelSkin(
    val brand: Color,
    val iconRes: Int? = null,
    val symbol: String? = null
)

/**
 * 账户渠道图标。
 *
 * 微信、支付宝和几家银行用的是 Arcticons 提供的官方 logo 轮廓（CC BY-SA 4.0，
 * 见 NOTICE），单色描边，按品牌色上色，容器是同一品牌的低透明底。
 * 交通银行没有现成 logo，退回 Material Symbols 的通用银行图标。
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
        val res = skin.iconRes
        if (res != null) {
            // 图标本身已经烤进品牌色，这里不能再 tint，否则会被主题色盖掉
            Icon(
                painter = painterResource(res),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(size * 0.62f)
            )
        } else {
            SymbolText(
                name = skin.symbol ?: "account_balance",
                size = size * 0.52f,
                color = skin.brand
            )
        }
    }
}

private fun channelSkin(channel: String?): ChannelSkin = when (channel) {
    "wechat" -> ChannelSkin(Color(0xFF07C160), R.drawable.ic_channel_wechat)
    "alipay" -> ChannelSkin(Color(0xFF1677FF), R.drawable.ic_channel_alipay)
    "icbc" -> ChannelSkin(Color(0xFFC7000B), R.drawable.ic_channel_icbc)
    "boc" -> ChannelSkin(Color(0xFFA6093D), R.drawable.ic_channel_boc)
    "cmb" -> ChannelSkin(Color(0xFFD3001D), R.drawable.ic_channel_cmb)
    "ccb" -> ChannelSkin(Color(0xFF0055A5), R.drawable.ic_channel_ccb)
    "abc" -> ChannelSkin(Color(0xFF009B4C), R.drawable.ic_channel_abc)
    "bocom" -> ChannelSkin(Color(0xFF005BAC), symbol = "account_balance")
    "cash" -> ChannelSkin(Color(0xFF00875F), R.drawable.ic_channel_cash)
    "credit" -> ChannelSkin(Color(0xFF7A4FCF), R.drawable.ic_channel_credit)
    else -> ChannelSkin(Color(0xFF5C6470), R.drawable.ic_channel_other)
}
