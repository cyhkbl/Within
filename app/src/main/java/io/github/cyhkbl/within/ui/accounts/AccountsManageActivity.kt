package io.github.cyhkbl.within.ui.accounts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import io.github.cyhkbl.within.util.MoneyUtils
import kotlinx.coroutines.launch

private data class AccountType(val key: String, val label: String, val icon: String)

private val accountTypes = listOf(
    AccountType("CASH", "现金", "payments"),
    AccountType("WALLET", "钱包", "account_balance_wallet"),
    AccountType("BANK", "银行卡", "account_balance"),
    AccountType("CREDIT", "信用卡", "credit_card"),
    AccountType("INVEST", "投资", "trending_up")
)

class AccountsManageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                AccountsManageScreen(repo) { finish() }
            }
        }
    }
}

@Composable
private fun AccountsManageScreen(repo: LedgerRepository, onBack: () -> Unit) {
    val accounts by repo.accounts().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<AccountEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<AccountEntity?>(null) }
    val total = accounts.filter { it.includeInTotal }.sumOf { it.balance }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 26.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
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
                        Text("返回")
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("账户管理", style = MaterialTheme.typography.titleMedium)
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "净资产",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        MoneyUtils.format(total),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(accounts, key = { it.id }) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { editing = account; showDialog = true }
                            .padding(horizontal = 6.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryGlyph(
                            icon = accountTypes.firstOrNull { it.key == account.type }?.icon ?: "wallet",
                            size = 42.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                accountTypes.firstOrNull { it.key == account.type }?.label ?: account.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            MoneyUtils.format(account.balance),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { pendingDelete = account }) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 60.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    if (showDialog) {
        AccountEditorDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { entity ->
                showDialog = false
                scope.launch { repo.saveAccount(entity) }
            }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除账户") },
            text = { Text("确定删除「${target.name}」？已经记在这上面的账单不会消失，账户余额也不再计入净资产。") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = null
                        scope.launch { repo.saveAccount(target.copy(archived = true)) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun AccountEditorDialog(
    initial: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "CASH") }
    var balanceText by remember {
        mutableStateOf(MoneyUtils.formatPlain(initial?.balance ?: 0L))
    }
    val isNew = initial == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "新建账户" else "编辑账户") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                    label = { Text(if (isNew) "初始余额" else "余额") },
                    prefix = { Text("¥") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "类型",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accountTypes) { t ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (t.key == type) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { type = t.key }
                        ) {
                            Text(
                                t.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (t.key == type) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cents = balanceText.toDoubleOrNull()?.let { Math.round(it * 100) } ?: 0L
                    val base = initial ?: AccountEntity(name = name, type = type)
                    onSave(
                        base.copy(
                            name = name.trim().ifBlank { "未命名账户" },
                            type = type,
                            balance = cents
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
