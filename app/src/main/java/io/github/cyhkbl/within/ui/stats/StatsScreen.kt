package io.github.cyhkbl.within.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.util.DateUtils
import io.github.cyhkbl.within.util.MoneyUtils
import java.time.YearMonth

@Composable
fun StatsScreen(repo: LedgerRepository) {
    val ym = remember { YearMonth.now() }
    val range = remember(ym) { DateUtils.monthRange(ym) }
    val txs by repo.transactions(range.first, range.second).collectAsState(initial = emptyList())
    val cats by repo.allCategories().collectAsState(initial = emptyList())

    val catMap = cats.associateBy { it.id }
    val expenses = txs.filter { it.kind == "EXPENSE" && !it.excludeFromStats }
    val refundSum = txs.filter { it.refundedFromId != null }.sumOf { it.amount }
    val total = (expenses.sumOf { it.amount } - refundSum).coerceAtLeast(0L)
    val byCategory = expenses.groupBy { it.categoryId ?: 0L }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text("统计", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    DateUtils.formatMonthLabel(ym),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        "本月总支出",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        MoneyUtils.format(total),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "共 ${expenses.size} 笔 · 日均 " + MoneyUtils.format(
                            if (expenses.isEmpty()) 0 else total / ym.lengthOfMonth().coerceAtLeast(1)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                    )
                }
            }
        }

        item {
            Text(
                "分类排行",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (byCategory.isEmpty()) {
            item {
                Text(
                    "这个月还没有支出",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(byCategory.size) { index ->
            val (catId, amount) = byCategory[index]
            CategoryBar(
                category = catMap[catId],
                amount = amount,
                total = total,
                rank = index + 1
            )
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun CategoryBar(category: CategoryEntity?, amount: Long, total: Long, rank: Int) {
    val ratio = if (total <= 0) 0f else amount.toFloat() / total
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(22.dp)
        )
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            SymbolText(
                name = category?.icon ?: "category",
                size = 20.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    category?.name ?: "未分类",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    MoneyUtils.format(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "${(ratio * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )
    }
}

