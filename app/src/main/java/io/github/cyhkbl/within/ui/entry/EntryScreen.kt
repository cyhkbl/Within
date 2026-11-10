package io.github.cyhkbl.within.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.cyhkbl.within.util.MoneyUtils
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.AmountDisplay
import io.github.cyhkbl.within.util.DateUtils
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

@Composable
fun EntryScreen(
    state: EntryUiState,
    onKindChange: (String) -> Unit,
    onCategory: (Long) -> Unit,
    onKey: (String) -> Unit,
    onNote: (String) -> Unit,
    onAccount: (Long) -> Unit,
    onTimestamp: (Long) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    var showAccountPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val amountText = if (state.amountInput.isEmpty()) "0.00" else state.amountInput

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── 顶部：关闭 + 收支切换 + 新增分类 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                SymbolText("close", size = 24.dp)
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KindTab(stringResource(R.string.entry_00), "EXPENSE", state.kind, onKindChange)
                Spacer(Modifier.width(6.dp))
                KindTab(stringResource(R.string.entry_01), "INCOME", state.kind, onKindChange)
                Spacer(Modifier.width(6.dp))
                KindTab(stringResource(R.string.entry_02), "TRANSFER", state.kind, onKindChange)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                SymbolText("add_circle", size = 24.dp)
            }
        }

        // ── 分类网格 ──
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp
            )
        ) {
            items(state.categories, key = { it.id }) { cat ->
                CategoryCell(cat, cat.id == state.categoryId) { onCategory(cat.id) }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── 备注 + 金额 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (state.note.isEmpty()) {
                    Text(
                        stringResource(R.string.entry_03),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = state.note,
                    onValueChange = onNote,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 76.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = amountText,
                style = AmountDisplay.copy(fontSize = 32.sp, lineHeight = 38.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ── 账户 / 时间 chips ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val account = state.accounts.firstOrNull { it.id == state.accountId }
            AssistChip(
                text = account?.name ?: stringResource(R.string.entry_04),
                icon = "account_balance_wallet",
                onClick = { showAccountPicker = true }
            )
            AssistChip(
                text = DateUtils.formatDateTime(state.timestamp).substring(5),
                icon = "schedule",
                onClick = { showTimePicker = true }
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── 数字键盘 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(260.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KeypadRow(listOf("1", "2", "3"), onKey)
                KeypadRow(listOf("4", "5", "6"), onKey)
                KeypadRow(listOf("7", "8", "9"), onKey)
                KeypadRow(listOf(".", "0", "00"), onKey)
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.width(96.dp)) {
                KeyButton(
                    label = "DEL",
                    isIcon = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(4.dp),
                    onClick = { onKey("DEL") }
                )
                val amountForSave = state.amountInput.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = onSave,
                    enabled = amountForSave > 0.0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.entry_06), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showAccountPicker) {
            AlertDialog(
                onDismissRequest = { showAccountPicker = false },
                title = { Text(stringResource(R.string.entry_04)) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        state.accounts.forEach { acc ->
                            TextButton(
                                onClick = {
                                    onAccount(acc.id)
                                    showAccountPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(acc.name, modifier = Modifier.weight(1f))
                                Text(MoneyUtils.format(acc.balance))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAccountPicker = false }) { Text(stringResource(R.string.common_cancel)) }
                }
            )
        }

        if (showTimePicker) {
            val dateState = rememberDatePickerState(initialSelectedDateMillis = state.timestamp)
            @OptIn(ExperimentalMaterial3Api::class)
            DatePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            val zone = java.time.ZoneId.systemDefault()
                            val old = DateUtils.toLocalDateTime(state.timestamp)
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(zone).toLocalDate()
                            onTimestamp(date.atTime(old.toLocalTime()).atZone(zone).toInstant().toEpochMilli())
                        }
                        showTimePicker = false
                    }) { Text(stringResource(R.string.common_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.common_cancel)) }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
    }
}

@Composable
private fun KindTab(label: String, kind: String, current: String, onChange: (String) -> Unit) {
    val selected = current == kind
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onChange(kind) }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryCell(cat: CategoryEntity, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            contentAlignment = Alignment.Center
        ) {
            SymbolText(
                name = cat.icon,
                size = 24.dp,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = cat.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AssistChip(text: String, icon: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SymbolText(icon, size = 16.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (text.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(keys: List<String>, onKey: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        keys.forEach { key ->
            KeyButton(
                label = key,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(4.dp)
            ) { onKey(key) }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    isIcon: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isIcon) {
                SymbolText("backspace", size = 22.dp)
            } else {
                Text(
                    text = label,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
