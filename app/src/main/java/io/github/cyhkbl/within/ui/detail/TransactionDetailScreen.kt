package io.github.cyhkbl.within.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.AmountDisplay
import io.github.cyhkbl.within.ui.theme.ExpenseRed
import io.github.cyhkbl.within.ui.theme.IncomeGreen
import io.github.cyhkbl.within.util.DateUtils
import io.github.cyhkbl.within.util.MoneyUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionDetailScreen(
    repo: LedgerRepository,
    txId: Long,
    onBack: () -> Unit
) {
    var reloadTick by remember { mutableStateOf(0) }
    val tx by produceState<TransactionEntity?>(initialValue = null, txId, reloadTick) {
        value = repo.transactionById(txId)
    }
    val category by produceState<CategoryEntity?>(initialValue = null, tx) {
        value = tx?.categoryId?.let { repo.categoryById(it) }
    }
    val account by produceState<AccountEntity?>(initialValue = null, tx) {
        value = tx?.accountId?.let { repo.accountById(it) }
    }
    val refunded by produceState<Long>(initialValue = 0L, txId, reloadTick) {
        value = repo.refundedSum(txId)
    }
    val refunds by produceState<List<TransactionEntity>>(initialValue = emptyList(), txId, reloadTick) {
        value = repo.refundsOf(txId)
    }

    val refundNotePrefix = stringResource(R.string.detail_26)
    var confirmedDelete by remember { mutableStateOf(false) }
    var showRefundDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val current = tx
    if (current == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.detail_00), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val color = when (current.kind) {
        "INCOME" -> IncomeGreen
        "TRANSFER" -> MaterialTheme.colorScheme.tertiary
        else -> ExpenseRed
    }
    val refundable = current.kind == "EXPENSE" && (current.amount - refunded) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
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
                    Text(stringResource(R.string.detail_01))
                }
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.detail_02), style = MaterialTheme.typography.titleMedium)
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryGlyph(icon = category?.icon ?: "category", size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(category?.name ?: stringResource(R.string.detail_03), style = MaterialTheme.typography.titleLarge)
                        Text(
                            when (current.kind) {
                                "INCOME" -> stringResource(R.string.detail_04)
                                "TRANSFER" -> stringResource(R.string.detail_05)
                                else -> stringResource(R.string.detail_06)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = when (current.kind) {
                        "INCOME" -> "+" + MoneyUtils.format(current.amount)
                        "TRANSFER" -> MoneyUtils.format(current.amount)
                        else -> "-" + MoneyUtils.format(current.amount)
                    },
                    style = AmountDisplay,
                    color = color
                )
                if (refunded > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.detail_07) + MoneyUtils.format(refunded) + stringResource(R.string.detail_08) +
                            MoneyUtils.format(current.amount - refunded),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                DetailRow(stringResource(R.string.detail_09), account?.name ?: "-")
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                DetailRow(stringResource(R.string.detail_10), DateUtils.formatDateTime(current.timestamp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                DetailRow(stringResource(R.string.detail_11), current.note.ifBlank { stringResource(R.string.detail_12) })
                if (current.source != "MANUAL") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    DetailRow(
                        stringResource(R.string.detail_13),
                        when (current.source) {
                            "REFUND" -> stringResource(R.string.detail_14)
                            "IMPORT" -> buildString {
                                append(stringResource(R.string.detail_15))
                                current.importBatchId?.take(8)?.let { append(" · #$it") }
                            }
                            "PERIODIC" -> stringResource(R.string.detail_16)
                            "AUTO" -> stringResource(R.string.detail_17) + (current.sourcePackage?.let { " · $it" } ?: "")
                            else -> current.source
                        }
                    )
                }
            }
        }

        if (refunds.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.detail_18),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    refunds.forEachIndexed { index, refund ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SymbolText(
                                "undo",
                                size = 20.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    MoneyUtils.format(refund.amount),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    DateUtils.formatDateTime(refund.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        repo.deleteTransaction(refund)
                                        reloadTick++
                                    }
                                }
                            ) { Text(stringResource(R.string.detail_19)) }
                        }
                        if (index != refunds.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 48.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (refundable) {
            Button(
                onClick = { showRefundDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                SymbolText("undo", size = 20.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.detail_20), fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (confirmedDelete) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.detail_21),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = { confirmedDelete = false }) { Text(stringResource(R.string.detail_22)) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            repo.deleteTransaction(current)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.detail_23)) }
            }
        } else {
            Button(
                onClick = { confirmedDelete = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.detail_24), fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(40.dp))
    }

    if (showRefundDialog) {
        RefundDialog(
            maxRefundable = current.amount - refunded,
            onDismiss = { showRefundDialog = false },
            onConfirm = { cents ->
                showRefundDialog = false
                scope.launch {
                    val refundCategory = repo.categoryByName("退款", "INCOME")
                    repo.insertTransaction(
                        TransactionEntity(
                            kind = "INCOME",
                            amount = cents,
                            categoryId = refundCategory?.id,
                            accountId = current.accountId,
                            note = refundNotePrefix + (current.note.ifBlank { category?.name ?: "" }),
                            timestamp = System.currentTimeMillis(),
                            source = "REFUND",
                            refundedFromId = current.id,
                            // 退款不增加收入，而是冲抵支出（钱迹的语义）
                            excludeFromStats = true
                        )
                    )
                    reloadTick++
                }
            }
        )
    }
}

@Composable
private fun RefundDialog(
    maxRefundable: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var input by remember { mutableStateOf(MoneyUtils.formatPlain(maxRefundable)) }
    val cents = input.toDoubleOrNull()?.let { Math.round(it * 100) } ?: 0L
    val valid = cents in 1..maxRefundable

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_27)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.detail_28) + MoneyUtils.format(maxRefundable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    prefix = { Text("¥") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!valid && input.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.detail_29),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (valid) onConfirm(cents) }, enabled = valid) {
                Text(stringResource(R.string.detail_30))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.detail_31)) }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
