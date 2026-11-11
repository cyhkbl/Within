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
import io.github.cyhkbl.within.data.AccountChannels

/**
 * 账户渠道图标：实心品牌色圆 + 白色图形，和钱迹、各家 App 的图标是同一个套路。
 */
@Composable
fun ChannelGlyph(channel: String?, size: Dp = 44.dp) {
    val spec = AccountChannels.byKey(channel)
    val brand = Color(spec?.brand ?: 0xFF7A4FCF)
    val drawable = drawableFor(channel)
    val symbol = symbolFor(channel)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(brand),
        contentAlignment = Alignment.Center
    ) {
        if (drawable != null) {
            Icon(
                painter = painterResource(drawable),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.58f)
            )
        } else {
            SymbolText(
                name = symbol,
                size = size * 0.5f,
                color = Color.White
            )
        }
    }
}

/** 有官方 logo 轮廓的渠道。 */
private fun drawableFor(channel: String?): Int? = when (channel) {
    "wechat" -> R.drawable.ic_channel_wechat
    "alipay" -> R.drawable.ic_channel_alipay
    "icbc" -> R.drawable.ic_channel_icbc
    "boc" -> R.drawable.ic_channel_boc
    "cmb" -> R.drawable.ic_channel_cmb
    "ccb" -> R.drawable.ic_channel_ccb
    "abc" -> R.drawable.ic_channel_abc
    "cash" -> R.drawable.ic_channel_cash
    "credit", "huabei", "jiebei", "baitiao" -> R.drawable.ic_channel_credit
    "other" -> R.drawable.ic_channel_other
    else -> null
}

/** 没有 logo 的渠道用 Material Symbols 近似。 */
private fun symbolFor(channel: String?): String = when (channel) {
    "wechat_change" -> "savings"
    "yuebao", "yulibao" -> "savings"
    "bank" -> "credit_card"
    "bocom" -> "account_balance"
    "qq" -> "chat"
    "phone" -> "smartphone"
    "utility" -> "bolt"
    "meal_card" -> "restaurant"
    "deposit" -> "savings"
    else -> "account_balance_wallet"
}
