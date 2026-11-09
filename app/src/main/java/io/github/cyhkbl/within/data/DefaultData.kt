package io.github.cyhkbl.within.data

import androidx.room.withTransaction
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.BookEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.LedgerDatabase
import io.github.cyhkbl.within.data.db.TransactionEntity

/** 首次启动时写入的默认账本、账户和分类。 */

object DefaultData {

    private data class SeedCategory(val name: String, val icon: String)

    private val expenseCategories = listOf(
        SeedCategory("三餐", "restaurant"),
        SeedCategory("零食", "cookie"),
        SeedCategory("衣服", "checkroom"),
        SeedCategory("交通", "directions_bus"),
        SeedCategory("旅行", "flight"),
        SeedCategory("孩子", "child_care"),
        SeedCategory("宠物", "pets"),
        SeedCategory("话费网费", "call"),
        SeedCategory("烟酒", "wine_bar"),
        SeedCategory("学习", "menu_book"),
        SeedCategory("日用品", "shopping_basket"),
        SeedCategory("住房", "home"),
        SeedCategory("美妆", "face_retouching_natural"),
        SeedCategory("医疗", "local_hospital"),
        SeedCategory("发红包", "card_giftcard"),
        SeedCategory("汽车加油", "directions_car"),
        SeedCategory("娱乐", "sports_esports"),
        SeedCategory("请客送礼", "celebration"),
        SeedCategory("电器数码", "devices"),
        SeedCategory("运动", "fitness_center"),
        SeedCategory("水电煤", "bolt"),
        SeedCategory("其它", "category")
    )

    private val incomeCategories = listOf(
        SeedCategory("工资", "work"),
        SeedCategory("奖金", "emoji_events"),
        SeedCategory("外快", "trending_up"),
        SeedCategory("理财", "savings"),
        SeedCategory("红包", "card_giftcard"),
        SeedCategory("退款", "undo"),
        SeedCategory("其它", "category")
    )

    private val accounts = listOf(
        Triple("现金", "CASH", "payments"),
        Triple("微信", "WALLET", "chat"),
        Triple("支付宝", "WALLET", "account_balance_wallet"),
        Triple("银行卡", "BANK", "account_balance")
    )

    suspend fun seedIfEmpty(db: LedgerDatabase) {
        if (db.categoryDao().count() > 0) return

        db.withTransaction {
            db.bookDao().insert(BookEntity(id = 1, name = "日常账本"))

            var order = 0
            expenseCategories.forEach { seed ->
                db.categoryDao().insert(
                    CategoryEntity(
                        name = seed.name,
                        kind = "EXPENSE",
                        icon = seed.icon,
                        sortOrder = order++
                    )
                )
            }
            order = 0
            incomeCategories.forEach { seed ->
                db.categoryDao().insert(
                    CategoryEntity(
                        name = seed.name,
                        kind = "INCOME",
                        icon = seed.icon,
                        sortOrder = order++
                    )
                )
            }

            var accountOrder = 0
            accounts.forEach { (name, type, icon) ->
                db.accountDao().insert(
                    AccountEntity(
                        name = name,
                        type = type,
                        icon = icon,
                        sortOrder = accountOrder++
                    )
                )
            }
        }
    }

    /** 给外部用：按名称猜分类（自动记账时用）。 */
    fun matchExpenseCategoryByKeyword(text: String): String? {
        val keywords = mapOf(
            "restaurant" to listOf("餐", "饭", "外卖", "食堂", "麦当劳", "肯德基", "星巴克", "奶茶", "咖啡"),
            "cookie" to listOf("零食", "水果", "超市", "便利店"),
            "directions_bus" to listOf("地铁", "公交", "打车", "滴滴", "高铁", "火车", "出租"),
            "shopping_basket" to listOf("淘宝", "京东", "拼多多", "超市", "日用品"),
            "sports_esports" to listOf("游戏", "视频", "会员", "音乐", "爱奇艺", "腾讯视频", "网易云"),
            "call" to listOf("话费", "流量", "宽带", "充值"),
            "local_hospital" to listOf("医院", "药", "挂号", "诊所"),
            "home" to listOf("房租", "物业", "水费", "电费", "燃气")
        )
        for ((icon, words) in keywords) {
            if (words.any { text.contains(it) }) return icon
        }
        return null
    }
}
