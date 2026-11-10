package io.github.cyhkbl.within.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.YearMonth

/**
 * 年月选择器。账单页和统计页共用，避免两边各写一份。
 */
@Composable
fun MonthPickerDialog(
    current: YearMonth,
    onDismiss: () -> Unit,
    onSelect: (YearMonth) -> Unit
) {
    var year by remember(current) { mutableStateOf(current.year) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { year -= 1 }) {
                    SymbolText("chevron_left", size = 22.dp)
                }
                Text(
                    "$year 年",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { year += 1 }) {
                    SymbolText("chevron_right", size = 22.dp)
                }
            }
        },
        text = {
            Column {
                for (rowIndex in 0 until 4) {
                    Row {
                        for (colIndex in 0 until 3) {
                            val monthValue = rowIndex * 3 + colIndex + 1
                            val selected = year == current.year && monthValue == current.monthValue
                            TextButton(
                                onClick = { onSelect(YearMonth.of(year, monthValue)) },
                                modifier = Modifier.weight(1f),
                                colors = if (selected) {
                                    ButtonDefaults.textButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    ButtonDefaults.textButtonColors()
                                }
                            ) { Text("$monthValue 月") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

