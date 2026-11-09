package io.github.cyhkbl.within.ui.components

/**
 * 符号名纠正表。
 *
 * 早期版本用了 `bus`、`child`、`wine` 这类简写，Material Symbols 里并没有这些 ligature，
 * 结果就是图标位置一片空白（有的还会渲染成半个字形，看着是「歪的」）。
 * 这里把旧名和老数据里的名字统一映射到真实存在的符号名，UI 层兜底，不必改数据库。
 */
object SymbolNames {

    private val aliases: Map<String, String> = mapOf(
        // 分类
        "bus" to "directions_bus",
        "child" to "child_care",
        "phone" to "call",
        "wine" to "wine_bar",
        "book" to "menu_book",
        "shopping" to "shopping_basket",
        "cosmetics" to "face_retouching_natural",
        "hospital" to "local_hospital",
        "gift" to "card_giftcard",
        "car" to "directions_car",
        "game" to "sports_esports",
        "party" to "celebration",
        "device" to "devices",
        "sport" to "fitness_center",
        "more" to "category",
        // 收入
        "salary" to "work",
        "bonus" to "emoji_events",
        "extra" to "trending_up",
        "invest" to "savings",
        "refund" to "undo",
        // 账户
        "cash" to "payments",
        "wechat" to "chat",
        "alipay" to "account_balance_wallet",
        "bank" to "account_balance",
        "wallet" to "account_balance_wallet",
        // 设置项
        "backup" to "cloud_upload",
        "label" to "label",
        "palette" to "palette",
        "info" to "info"
    )

    fun resolve(name: String): String = aliases[name] ?: name
}
