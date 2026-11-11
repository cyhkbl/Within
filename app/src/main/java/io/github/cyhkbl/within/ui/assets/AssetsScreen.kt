package io.github.cyhkbl.within.ui.assets

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeFloatingActionButton
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
import io.github.cyhkbl.within.data.AccountChannel
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
 * 新建账户分两步（先挑渠道、再填名称和余额），和钱迹一样；
 * 编辑已有账户时渠道可以换，余额改动会生成一笔「余额调整」。
 */
@Composable
fun AssetsScreen(repo: LedgerRepository) {
    val accounts by repo.accounts().collectAsState(initial = emptyList())
    val total by repo.totalBalance().collectAsState(initial = 0L)
    val scope = rememberCoroutineScope()

    var editorTarget by remember { mutableStateOf<AccountEntity?>(null) }
    var creatingWithChannel by remember { mutableStateOf<AccountChannel?>(null) }
    var pickingChannelFor by remember { mutableStateOf<AccountEntity?>(null) }
    var showChannelPicker by remember { mutableStateOf(false) }
    var showCreatePicker by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<AccountEntity?>(null) }
    var toast by remember { mutableStateOf("") }

    LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            kotlinx.coroutines.delay(2600)
            toast = ""
        }
    }

    // ── 新建：第一步选渠道 ──
    if (showCreatePicker) {
        ChannelPickerDialog(
            title = "选择资产分类",
            onDismiss = { showCreatePicker = false },
            onPick = { channel ->
                showCreatePicker = false
                creatingWithChannel = channel
            }
        )
    }

    // ── 新建：第二步填信息 ──
    creatingWithChannel?.let { channel ->
        AccountEditDialog(
            initial = null,
            channel = channel,
            existingNames = accounts.map { it.name },
            onChangeChannel = {
                creatingWithChannel = null
                showCreatePicker = true
            },
            onDismiss = { creatingWithChannel = null },
            onSave = { entity ->
                creatingWithChannel = null
                scope.launch {
                    repo.saveAccount(entity)
                    toast = "已添加「${entity.name}」"
                }
            },
            onDelete = null
        )
    }

    // ── 编辑已有账户 ──
    editorTarget?.let { account ->
        AccountEditDialog(
            initial = account,
            channel = AccountChannels.byKey(account.type) ?: AccountChannels.all.last(),
            existingNames = accounts.map { it.name },
            onChangeChannel = {
                pickingChannelFor = account
                editorTarget = null
                showChannelPicker = true
            },
            onDismiss = { editorTarget = null },
            onSave = { entity ->
                editorTarget = null
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
                editorTarget = null
                pendingDelete = account
            }
        )
    }

    if (showChannelPicker) {
        ChannelPickerDialog(
            title = "更换渠道",
            onDismiss = {
                showChannelPicker = false
                pickingChannelFor = null
            },
            onPick = { channel ->
                showChannelPicker = false
                val target = pickingChannelFor
                pickingChannelFor = null
                if (target != null) {
                    editorTarget = target.copy(type = channel.key)
                }
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
                onClick = { showCreatePicker = true },
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
                                    .clickable { editorTarget = account }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChannelGlyph(channel = account.type, size = 44.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(account.name, style = MaterialTheme.typography.titleMedium)
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

/** 渠道选择：一级按资金 / 信用 / 充值 / 投资 / 债务分组，有子项的（如银行卡）再进一层。 */
@Composable
private fun ChannelPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onPick: (AccountChannel) -> Unit
) {
    var expanded by remember { mutableStateOf<AccountChannel?>(null) }
    val scrollState = rememberScrollState()
    LaunchedEffect(title, expanded) { scrollState.scrollTo(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                expanded?.label ?: title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
            ) {
                val current = expanded
                if (current != null) {
                    ChannelGrid(
                        channels = AccountChannels.childrenOf(current.key),
                        onPick = { onPick(it) }
                    )
                } else {
                    listOf(
                        AccountChannels.FUND to AccountChannels.fundChannels,
                        AccountChannels.CREDIT to AccountChannels.creditChannels,
                        AccountChannels.RECHARGE to AccountChannels.rechargeChannels,
                        AccountChannels.INVEST to AccountChannels.investChannels,
                        AccountChannels.DEBT to AccountChannels.debtChannels
                    ).forEach { (group, channels) ->
                        Text(
                            AccountChannels.groupLabel(group),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))
                        ChannelGrid(
                            channels = channels,
                            onPick = { option ->
                                if (AccountChannels.hasChildren(option.key)) {
                                    expanded = option
                                } else {
                                    onPick(option)
                                }
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = {
                if (expanded != null) expanded = null else onDismiss()
            }) {
                Text(if (expanded != null) "返回" else "取消")
            }
        }
    )
}

/** 4 列图标网格，有子项的右下角带一个小箭头。 */
@Composable
private fun ChannelGrid(
    channels: List<AccountChannel>,
    onPick: (AccountChannel) -> Unit
) {
    Column {
        channels.chunked(4).forEach { rowChannels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowChannels.forEach { option ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPick(option) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            ChannelGlyph(channel = option.key, size = 44.dp)
                            if (AccountChannels.hasChildren(option.key)) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    SymbolText(
                                        "chevron_right",
                                        size = 13.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                repeat(4 - rowChannels.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** 填名称和余额。渠道单独显示，点一下能换。 */
@Composable
private fun AccountEditDialog(
    initial: AccountEntity?,
    channel: AccountChannel,
    existingNames: List<String>,
    onChangeChannel: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val isNew = initial == null
    var name by remember(initial, channel) {
        mutableStateOf(
            initial?.name ?: AccountChannels.nextName(
                channel.key,
                existingNames
            )
        )
    }
    var balanceText by remember(initial) {
        mutableStateOf(MoneyUtils.formatPlain(initial?.balance ?: 0L))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "新建账户" else "编辑账户") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onChangeChannel)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChannelGlyph(channel = channel.key, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(channel.label, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "点击更换渠道",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SymbolText(
                        "chevron_right",
                        size = 20.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(14.dp))
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
            }
        },
        confirmButton = {
            Button(onClick = {
                val balance = balanceText.toDoubleOrNull()?.let { Math.round(it * 100) } ?: 0L
                val finalName = name.trim().ifBlank { channel.label }
                onSave(
                    (initial ?: AccountEntity(name = finalName, type = channel.key)).copy(
                        name = finalName,
                        type = channel.key,
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
