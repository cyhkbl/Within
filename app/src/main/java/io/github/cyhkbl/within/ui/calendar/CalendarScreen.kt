package io.github.cyhkbl.within.ui.calendar

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.ExpenseRed
import io.github.cyhkbl.within.ui.theme.IncomeGreen
import io.github.cyhkbl.within.util.DateUtils
import io.github.cyhkbl.within.util.MoneyUtils
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

@Composable
fun CalendarScreen(
    repo: LedgerRepository,
    onBack: () -> Unit,
    onTxClick: (Long) -> Unit
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val range = remember(month) { DateUtils.monthRange(month) }
    val txs by repo.transactions(range.first, range.second).collectAsState(initial = emptyList())
    val cats by repo.allCategories().collectAsState(initial = emptyList())
    val catMap = remember(cats) { cats.associateBy { it.id } }

    val byDay = remember(txs) {
        txs.groupBy { DateUtils.toLocalDate(it.timestamp) }
    }

    val firstDay = month.atDay(1)
    val lead = firstDay.dayOfWeek.value - 1          // 周一为 0
    val daysInMonth = month.lengthOfMonth()
    val totalCells = ((lead + daysInMonth + 6) / 7) * 7

    val dayTx = selectedDay?.let { byDay[it] }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                OutlinedButton(
                    onClick = onBack,
                    border = null,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                ) {
                    SymbolText("arrow_back", size = 20.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.calendar_00))
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        month = month.minusMonths(1)
                        selectedDay = null
                    },
                contentAlignment = Alignment.Center
            ) { SymbolText("chevron_left", size = 24.dp) }
            Text(
                DateUtils.formatMonthLabel(month),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        month = month.plusMonths(1)
                        selectedDay = null
                    },
                contentAlignment = Alignment.Center
            ) { SymbolText("chevron_right", size = 24.dp) }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(stringResource(R.string.calendar_01), stringResource(R.string.calendar_02), stringResource(R.string.calendar_03), stringResource(R.string.calendar_04), stringResource(R.string.calendar_05), stringResource(R.string.calendar_06), stringResource(R.string.calendar_07)).forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        for (rowIndex in 0 until (totalCells / 7)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = rowIndex * 7 + col
                    val dayNumber = cellIndex - lead + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = month.atDay(dayNumber)
                        DayCell(
                            day = dayNumber,
                            date = date,
                            txs = byDay[date].orEmpty(),
                            selected = selectedDay == date,
                            isToday = date == LocalDate.now(),
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedDay = if (selectedDay == date) null else date
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (selectedDay != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    DateUtils.formatDayHeader(DateUtils.fromLocalDateTime(selectedDay!!.atStartOfDay())),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                val expense = dayTx.filter { it.kind == "EXPENSE" }.sumOf { it.amount }
                val income = dayTx.filter { it.kind == "INCOME" }.sumOf { it.amount }
                Text(
                    buildList {
                        if (income > 0) add(stringResource(R.string.calendar_08) + MoneyUtils.format(income))
                        if (expense > 0) add(stringResource(R.string.calendar_09) + MoneyUtils.format(expense))
                    }.joinToString("  "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))

            if (dayTx.isEmpty()) {
                Text(
                    stringResource(R.string.calendar_10),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        dayTx.sortedByDescending { it.timestamp }.forEachIndexed { index, tx ->
                            DayTxRow(tx, catMap[tx.categoryId]) { onTxClick(tx.id) }
                            if (index != dayTx.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 64.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun DayCell(
    day: Int,
    date: LocalDate,
    txs: List<TransactionEntity>,
    selected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val expense = txs.filter { it.kind == "EXPENSE" }.sumOf { it.amount }
    val income = txs.filter { it.kind == "INCOME" }.sumOf { it.amount }

    Column(
        modifier = modifier
            .padding(2.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    selected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
        if (expense > 0) {
            Text(
                text = compact(expense),
                fontSize = 9.sp,
                color = ExpenseRed,
                maxLines = 1
            )
        }
        if (income > 0) {
            Text(
                text = compact(income),
                fontSize = 9.sp,
                color = IncomeGreen,
                maxLines = 1
            )
        }
    }
}

private fun compact(cents: Long): String {
    val yuan = cents / 100.0
    return when {
        yuan >= 10000 -> (yuan / 10000).toInt().toString() + "w"
        yuan >= 1000 -> (yuan / 1000).toInt().toString() + "k"
        yuan >= 100 -> yuan.toInt().toString()
        else -> String.format(java.util.Locale.getDefault(), "%.0f", yuan)
    }
}

@Composable
private fun DayTxRow(tx: TransactionEntity, category: CategoryEntity?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryGlyph(icon = category?.icon ?: "category", size = 38.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category?.name ?: stringResource(R.string.calendar_11), style = MaterialTheme.typography.bodyLarge)
            if (tx.note.isNotBlank()) {
                Text(
                    tx.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Text(
            text = when (tx.kind) {
                "INCOME" -> "+" + MoneyUtils.format(tx.amount)
                "TRANSFER" -> MoneyUtils.format(tx.amount)
                else -> "-" + MoneyUtils.format(tx.amount)
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = when (tx.kind) {
                "INCOME" -> IncomeGreen
                "TRANSFER" -> MaterialTheme.colorScheme.tertiary
                else -> ExpenseRed
            }
        )
    }
}
