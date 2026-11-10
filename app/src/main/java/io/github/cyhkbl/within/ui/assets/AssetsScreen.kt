package io.github.cyhkbl.within.ui.assets

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.util.MoneyUtils
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

@Composable
fun AssetsScreen(repo: LedgerRepository) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val accounts by repo.accounts().collectAsState(initial = emptyList())
    val total by repo.totalBalance().collectAsState(initial = 0L)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showCalibrate by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            kotlinx.coroutines.delay(2600)
            toast = ""
        }
    }

    if (showCalibrate) {
        CalibrateDialog(
            accounts = accounts,
            onDismiss = { showCalibrate = false },
            onConfirm = { targets ->
                showCalibrate = false
                scope.launch {
                    targets.forEach { (id, balance) -> repo.adjustAccountBalance(id, balance) }
                    toast = "余额已按钱迹校准"
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.assets_00), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "共 ${accounts.size} 个账户",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                android.content.Intent(context, io.github.cyhkbl.within.ui.accounts.AccountsManageActivity::class.java)
                            )
                        },
                        border = null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                    ) {
                        SymbolText("tune", size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.assets_01))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    OutlinedButton(
                        onClick = { showCalibrate = true },
                        border = null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                    ) {
                        SymbolText("balance", size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("校准")
                    }
                }
            }
        }

        if (toast.isNotEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Text(
                        toast,
                        modifier = androidx.compose.ui.Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
                        stringResource(R.string.assets_02),
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
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    accounts.forEachIndexed { index, account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryGlyph(icon = accountIcon(account.type), size = 42.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(account.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    accountTypeLabel(account.type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                MoneyUtils.format(account.balance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (index != accounts.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 74.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

private fun accountIcon(type: String): String = when (type) {
    "BANK" -> "account_balance"
    "CREDIT" -> "credit_card"
    "WALLET" -> "account_balance_wallet"
    "INVEST" -> "trending_up"
    "CASH" -> "payments"
    else -> "wallet"
}

@Composable
private fun accountTypeLabel(type: String): String = when (type) {
    "BANK" -> stringResource(R.string.assets_03)
    "CREDIT" -> stringResource(R.string.assets_04)
    "WALLET" -> stringResource(R.string.assets_05)
    "INVEST" -> stringResource(R.string.assets_06)
    "CASH" -> stringResource(R.string.assets_07)
    else -> stringResource(R.string.assets_08)
}


@Composable
private fun CalibrateDialog(
    accounts: List<io.github.cyhkbl.within.data.db.AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Map<Long, Long>) -> Unit
) {
    val inputs = androidx.compose.runtime.remember(accounts) {
        androidx.compose.runtime.mutableStateMapOf<Long, String>().apply {
            accounts.forEach { put(it.id, io.github.cyhkbl.within.util.MoneyUtils.formatPlain(it.balance)) }
        }
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("按钱迹的余额校准") },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.verticalScroll(
                    androidx.compose.foundation.rememberScrollState()
                )
            ) {
                androidx.compose.material3.Text(
                    "填每个账户在钱迹里显示的余额。差额会记成一条「余额调整」，只改余额，不计入收支统计。",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(10.dp))
                accounts.forEach { acc ->
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            acc.name,
                            modifier = androidx.compose.ui.Modifier.weight(1f)
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = inputs[acc.id] ?: "",
                            onValueChange = { inputs[acc.id] = it },
                            singleLine = true,
                            modifier = androidx.compose.ui.Modifier.width(150.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            )
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val targets = accounts.associate { acc ->
                    val raw = inputs[acc.id]?.toDoubleOrNull() ?: 0.0
                    acc.id to Math.round(raw * 100)
                }
                onConfirm(targets)
            }) { androidx.compose.material3.Text("校准") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("取消")
            }
        }
    )
}
