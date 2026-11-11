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
 * 账户渠道图标：实心品牌色圆 + 白色图形。
 *
 * 微信和支付宝用的是官方 logo 的精确矢量（Simple Icons，CC0），
 * 其余渠道没有公开的矢量资源，用 Material Symbols 画出同类意象。
 */
@Composable
fun ChannelGlyph(channel: String?, size: Dp = 44.dp) {
    val spec = AccountChannels.byKey(channel)
    val brand = Color(spec?.brand ?: 0xFF7A4FCF)
    val drawable = drawableFor(channel)

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
                name = symbolFor(channel),
                size = size * 0.5f,
                color = Color.White
            )
        }
    }
}

/** 有精确官方 logo 的渠道。 */
private fun drawableFor(channel: String?): Int? = when (channel) {
    "wechat" -> R.drawable.ic_channel_wechat
    "alipay" -> R.drawable.ic_channel_alipay
    "cash" -> R.drawable.ic_channel_cash
    "other" -> R.drawable.ic_channel_other
    "credit", "huabei", "jiebei", "baitiao", "meituan", "douyin",
    "wechat_fenqi", "other_credit" -> R.drawable.ic_channel_credit
    else -> null
}

private fun symbolFor(channel: String?): String = when (channel) {
    "wechat_change" -> "savings"
    "yuebao", "yulibao", "purse" -> "savings"
    "yunshanfu" -> "qr_code_scanner"
    "bank" -> "credit_card"
    "housing_fund" -> "home"
    "qq" -> "chat"
    "jd", "pdd" -> "shopping_bag"
    "medical" -> "medical_services"
    "digital_cny" -> "currency_yen"
    "huawei" -> "wallet"
    "paypal" -> "account_balance_wallet"
    "phone" -> "smartphone"
    "utility" -> "bolt"
    "meal_card" -> "restaurant"
    "deposit" -> "lock"
    "bus_card" -> "directions_bus"
    "member_card" -> "card_membership"
    "fuel_card", "sinopec" -> "local_gas_station"
    "apple" -> "phone_iphone"
    "other_recharge" -> "credit_card"
    "stock" -> "trending_up"
    "fund" -> "show_chart"
    "gold" -> "paid"
    "forex" -> "currency_exchange"
    "futures" -> "candlestick_chart"
    "bond" -> "receipt_long"
    "fixed_income" -> "savings"
    "crypto" -> "currency_bitcoin"
    "other_invest" -> "pie_chart"
    "lend_out" -> "north_east"
    "borrow_in" -> "south_west"
    else -> "account_balance_wallet"
}
