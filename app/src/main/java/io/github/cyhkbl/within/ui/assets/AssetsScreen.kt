package io.github.cyhkbl.within.ui.assets

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.data.AccountChannels
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.ui.components.ChannelGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.util.MoneyUtils
import kotlinx.coroutines.launch

/**
 * 资产页。
 *
 * 账户类型就是渠道（微信、支付宝、工商银行……），点账户直接编辑，
 * 右下角新建时先选渠道，名称按渠道自动生成（微信 → 微信2）。
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
            existingNames = accounts.map { it.name },
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
            existingNames = accounts.map { it.name },
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
                        style = MaterialTheme.typography.displaySmall,
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
                        shape = MaterialTheme.shapes.large,
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
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            stringResource(R.string.assets_02),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            MoneyUtils.format(total),
                            fontSize = 40.sp,
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
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        accounts.forEachIndexed { index, account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editing = account }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChannelGlyph(channel = account.type, size = 44.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                                    // 名字和渠道一致时不重复显示（「支付宝 / 支付宝」）
                                    val channelLabel = AccountChannels.labelOf(account.type)
                                    if (account.name != channelLabel) {
                                        Text(
                                            channelLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    MoneyUtils.format(account.balance),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (index != accounts.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp),
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

@Composable
private fun AccountEditDialog(
    initial: AccountEntity?,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val isNew = initial == null
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var channel by remember { mutableStateOf(initial?.type ?: "wechat") }
    var balanceText by remember { mutableStateOf(MoneyUtils.formatPlain(initial?.balance ?: 0L)) }
    var nameTouched by remember { mutableStateOf(!isNew) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "新建账户" else "编辑账户") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("渠道", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                AccountChannels.all.chunked(3).forEach { rowChannels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowChannels.forEach { option ->
                            val selected = option.key == channel
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        channel = option.key
                                        // 用户没手动改过名字时，名字跟着渠道走
                                        if (!nameTouched) {
                                            name = AccountChannels.nextName(
                                                option.key,
                                                existingNames.filter { it != initial?.name }
                                            )
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ChannelGlyph(channel = option.key, size = 28.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        option.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        repeat(3 - rowChannels.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
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
            }
        },
        confirmButton = {
            Button(onClick = {
                val balance = balanceText.toDoubleOrNull()?.let { Math.round(it * 100) } ?: 0L
                val finalName = name.trim().ifBlank {
                    AccountChannels.nextName(channel, existingNames.filter { it != initial?.name })
                }
                onSave(
                    (initial ?: AccountEntity(name = finalName, type = channel)).copy(
                        name = finalName,
                        type = channel,
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
