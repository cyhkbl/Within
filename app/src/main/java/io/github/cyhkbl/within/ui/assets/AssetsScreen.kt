package io.github.cyhkbl.within.ui.assets

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.util.MoneyUtils

/**
 * 资产页。
 *
 * 不设二级管理页：点任意账户直接改余额，右下角 + 新建账户。
 * 改余额不会覆盖字段，而是生成一笔「余额调整」平账记录，保证流水和余额永远自洽。
 */
@Composable
fun AssetsScreen(repo: LedgerRepository) {
    val accounts by repo.accounts().collectAsState(initial = emptyList())
    val total by repo.totalBalance().collectAsState(initial = 0L)
    val scope = rememberCoroutineScope()

    var editing by remember { mutableStateOf<AccountEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<AccountEntity?>(null) }
    var toast by remember { mutableStateOf("") }

    LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            kotlinx.coroutines.delay(2600)
            toast = ""
        }
    }

    if (creating) {
        AccountEditDialog(
            initial = null,
            onDismiss = { creating = false },
            onSave = { entity ->
                creating = false
                scope.launch {
                    repo.saveAccount(entity)
                    toast = "已添加「${entity.name}」"
                }
            },
            onDelete = null
        )
    }

    editing?.let { account ->
        AccountEditDialog(
            initial = account,
            onDismiss = { editing = null },
            onSave = { entity ->
                editing = null
                scope.launch {
                    val old = repo.accountById(entity.id)
                    repo.saveAccount(entity.copy(balance = old?.balance ?: 0L))
                    if (old != null && old.balance != entity.balance) {
                        repo.adjustAccountBalance(entity.id, entity.balance)
                    }
                    toast = "「${entity.name}」已更新"
                }
            },
            onDelete = {
                editing = null
                pendingDelete = account
            }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.accounts_09)) },
            text = { Text("删除「${target.name}」之后，记在它上面的账单不会消失，但这个账户的余额不再计入净资产。") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = null
                        scope.launch {
                            repo.saveAccount(target.copy(archived = true))
                            toast = "已删除「${target.name}」"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.accounts_10)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.accounts_11))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { creating = true },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText(
                    "add",
                    size = 30.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                    Text(
                        stringResource(R.string.assets_00),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "共 ${accounts.size} 个账户 · 点一下就能改余额",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (toast.isNotEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            toast,
                            modifier = Modifier.padding(14.dp),
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
                                    .clickable { editing = account }
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

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

private data class AccountTypeOption(val key: String, val label: String, val icon: String)

private val accountTypeOptions = listOf(
    AccountTypeOption("CASH", "现金", "payments"),
    AccountTypeOption("WALLET", "钱包", "account_balance_wallet"),
    AccountTypeOption("BANK", "银行卡", "account_balance"),
    AccountTypeOption("CREDIT", "信用卡", "credit_card"),
    AccountTypeOption("INVEST", "投资", "trending_up")
)

@Composable
private fun AccountEditDialog(
    initial: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val isNew = initial == null
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "CASH") }
    var balanceText by remember { mutableStateOf(MoneyUtils.formatPlain(initial?.balance ?: 0L)) }
    val fallbackName = "未命名账户"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "新建账户" else "编辑账户") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text(if (isNew) "初始余额" else "余额") },
                    singleLine = true,
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (!isNew) {
                        { Text("改动会记成一笔「余额调整」，不计入收支") }
                    } else {
                        null
                    }
                )
                Spacer(Modifier.height(12.dp))
                Text("类型", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Column {
                    accountTypeOptions.chunked(3).forEach { rowOptions ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowOptions.forEach { option ->
                                val selected = option.key == type
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .clickable { type = option.key }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SymbolText(
                                            option.icon,
                                            size = 16.dp,
                                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            option.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val balance = balanceText.toDoubleOrNull()?.let { Math.round(it * 100) } ?: 0L
                onSave(
                    (initial ?: AccountEntity(name = name, type = type)).copy(
                        name = name.trim().ifBlank { fallbackName },
                        type = type,
                        balance = balance
                    )
                )
            }) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
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
