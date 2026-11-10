package io.github.cyhkbl.within.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.MiniBar
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.AmountDisplay
import io.github.cyhkbl.within.ui.theme.ExpenseRed
import io.github.cyhkbl.within.ui.theme.IncomeGreen
import io.github.cyhkbl.within.util.DateUtils
import io.github.cyhkbl.within.util.MoneyUtils
import java.time.YearMonth
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.ui.components.MonthPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onAdd: () -> Unit,
    onOpenCalendar: () -> Unit,
    onTxClick: (TransactionEntity) -> Unit
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthPickerDialog(
            current = state.month,
            onDismiss = { showMonthPicker = false },
            onSelect = { ym ->
                onSelectMonth(ym)
                showMonthPicker = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showMonthPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = DateUtils.formatMonthLabel(state.month),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        SymbolText(
                            "expand_more",
                            size = 22.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onPrevMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        SymbolText("chevron_left", size = 24.dp)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenCalendar),
                        contentAlignment = Alignment.Center
                    ) {
                        SymbolText("calendar_month", size = 22.dp)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onNextMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        SymbolText("chevron_right", size = 24.dp)
                    }
                    Spacer(Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAdd,
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 30.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "summary") { SummaryCard(state) }
            item(key = "week") { WeekCard(state) }
            if (state.groups.isEmpty()) {
                item(key = "empty") { EmptyState() }
            }
            state.groups.forEach { group ->
                item(key = "day-${group.dayKey}") { DayHeader(group) }
                items(group.items, key = { "tx-${it.id}" }) { tx ->
                    TransactionRow(
                        tx = tx,
                        category = state.categories[tx.categoryId],
                        account = state.accounts[tx.accountId],
                        onClick = { onTxClick(tx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: HomeUiState) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = if (state.month == YearMonth.now()) {
                    stringResource(R.string.home_00)
                } else {
                    stringResource(R.string.home_month_expense_fmt, state.month.monthValue)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = MoneyUtils.format(state.expense),
                style = AmountDisplay,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_01),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        MoneyUtils.format(state.income),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_02),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        MoneyUtils.format(state.income - state.expense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.home_03),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        MoneyUtils.format(state.totalBalance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekCard(state: HomeUiState) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_04), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.home_05) + MoneyUtils.format(state.weekTotal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SymbolText("bar_chart", size = 20.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            MiniBar(
                values = state.weekValues,
                labels = weekdayLabels(state.weekDays),
                barColor = MaterialTheme.colorScheme.primary,
                height = 120.dp
            )
        }
    }
}

@Composable
private fun DayHeader(group: DayGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        val parts = buildList {
            if (group.income > 0) add("收 ${MoneyUtils.format(group.income)}")
            if (group.expense > 0) add("支 ${MoneyUtils.format(group.expense)}")
        }
        Text(
            text = parts.joinToString("  "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    category: CategoryEntity?,
    account: AccountEntity?,
    onClick: () -> Unit
) {
    val color = when (tx.kind) {
        "INCOME" -> IncomeGreen
        "TRANSFER" -> MaterialTheme.colorScheme.tertiary
        else -> ExpenseRed
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryGlyph(icon = category?.icon ?: "category", size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category?.name ?: stringResource(R.string.home_06),
                style = MaterialTheme.typography.titleMedium
            )
            val sub = buildList {
                if (tx.note.isNotBlank()) add(tx.note)
                account?.let { add(it.name) }
                add(DateUtils.formatTime(tx.timestamp))
            }.joinToString(" · ")
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = when (tx.kind) {
                "INCOME" -> "+" + MoneyUtils.format(tx.amount)
                "TRANSFER" -> MoneyUtils.format(tx.amount)
                else -> "-" + MoneyUtils.format(tx.amount)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun EmptyState() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SymbolText("receipt_long", size = 48.dp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.home_07), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.home_08),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun weekdayLabels(days: List<Int>): List<String> {
    val names = androidx.compose.ui.platform.LocalContext.current.resources
        .getStringArray(R.array.weekday_short)
    return days.map { day -> names.getOrElse(day - 1) { "" } }
}
