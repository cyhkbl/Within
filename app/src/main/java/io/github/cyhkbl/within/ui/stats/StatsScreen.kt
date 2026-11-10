package io.github.cyhkbl.within.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.ui.components.MonthPickerDialog
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.util.DateUtils
import io.github.cyhkbl.within.util.MoneyUtils
import java.time.YearMonth

private enum class StatsMode { MONTH, YEAR }

/** 环状图和排行共用的配色，按顺序取。 */
private val sliceColors = listOf(
    Color(0xFF6750A4), Color(0xFF7D5260), Color(0xFF00696E), Color(0xFF8B5000),
    Color(0xFF3E6837), Color(0xFF984061), Color(0xFF0061A4), Color(0xFF6C5D00)
)

@Composable
fun StatsScreen(repo: LedgerRepository) {
    var mode by remember { mutableStateOf(StatsMode.MONTH) }
    var ym by remember { mutableStateOf(YearMonth.now()) }
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        MonthPickerDialog(
            current = ym,
            onDismiss = { showPicker = false },
            onSelect = { picked ->
                ym = picked
                showPicker = false
            }
        )
    }

    val range = remember(ym, mode) {
        if (mode == StatsMode.MONTH) DateUtils.monthRange(ym) else DateUtils.yearRange(ym.year)
    }
    val rawTxs by repo.transactions(range.first, range.second).collectAsState(initial = emptyList())
    val categories by repo.allCategories().collectAsState(initial = emptyList())

    // 余额调整不是真实消费，统计里不参与
    val txs = rawTxs.filter { it.source != "ADJUST" }
    val catMap = categories.associateBy { it.id }
    val expenses = txs.filter { it.kind == "EXPENSE" && !it.excludeFromStats }
    val total = expenses.sumOf { it.amount }

    val byCategory = expenses.groupBy { it.categoryId ?: 0L }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val slices = byCategory.take(8).mapIndexed { index, (id, amount) ->
        Slice(
            name = catMap[id]?.name ?: stringResource(R.string.stats_04),
            amount = amount,
            percent = if (total > 0) amount.toFloat() / total else 0f,
            color = sliceColors[index % sliceColors.size]
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.stats_00),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    ModeSwitch(mode) { mode = it }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { showPicker = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (mode == StatsMode.MONTH) {
                            DateUtils.formatMonthLabel(ym)
                        } else {
                            "${ym.year}年"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SymbolText(
                        "expand_more",
                        size = 16.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        if (mode == StatsMode.MONTH) "本月总支出" else "本年总支出",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        MoneyUtils.format(total),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (mode == StatsMode.MONTH) {
                            "共 ${expenses.size} 笔 · 日均 ${MoneyUtils.format(total / ym.lengthOfMonth().coerceAtLeast(1))}"
                        } else {
                            "共 ${expenses.size} 笔 · 月均 ${MoneyUtils.format(total / 12)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.stats_03),
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DonutChart(
                            slices = slices,
                            total = total,
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            stringResource(R.string.stats_02),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            items(byCategory.size) { index ->
                val (id, amount) = byCategory[index]
                val category = catMap[id]
                val percent = if (total > 0) amount.toFloat() / total else 0f
                val color = sliceColors[index % sliceColors.size]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            category?.name ?: stringResource(R.string.stats_04),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percent)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(color)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            MoneyUtils.format(amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${(percent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}

private data class Slice(
    val name: String,
    val amount: Long,
    val percent: Float,
    val color: Color
)

@Composable
private fun DonutChart(slices: List<Slice>, total: Long, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 26.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            var start = -90f
            slices.forEach { slice ->
                val sweep = (slice.percent * 360f)
                if (sweep > 0.2f) {
                    drawArc(
                        color = slice.color,
                        startAngle = start,
                        sweepAngle = sweep - 1.2f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                }
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (slices.isEmpty()) "—" else slices.first().name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (total > 0) "${(slices.first().percent * 100).toInt()}%" else "0%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModeSwitch(mode: StatsMode, onChange: (StatsMode) -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            ModeChip("月", mode == StatsMode.MONTH) { onChange(StatsMode.MONTH) }
            ModeChip("年", mode == StatsMode.YEAR) { onChange(StatsMode.YEAR) }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary
        else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
