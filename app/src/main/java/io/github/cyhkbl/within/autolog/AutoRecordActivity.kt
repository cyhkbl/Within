package io.github.cyhkbl.within.autolog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.AutoRecordGate
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.data.DefaultData
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import io.github.cyhkbl.within.util.MoneyUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

class AutoRecordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setFinishOnTouchOutside(true)
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                val draft = AutoRecordGate.draft.value
                if (draft == null) {
                    LaunchedEffect(Unit) { finish() }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable {
                                AutoRecordGate.clear()
                                finish()
                            }
                    ) {
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            AutoRecordPrompt(
                                draft = draft,
                                repo = repo,
                                onSaved = {
                                    AutoRecordGate.clear()
                                    finish()
                                },
                                onCancel = {
                                    AutoRecordGate.clear()
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoRecordPrompt(
    draft: PaymentDraft,
    repo: LedgerRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val kind = draft.kind
    val categories by repo.categoriesByKind(kind).collectAsState(initial = emptyList())
    val accounts by repo.accounts().collectAsState(initial = emptyList())

    var amountText by remember { mutableStateOf(MoneyUtils.formatPlain(draft.amount)) }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    var selectedAccount by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf(draft.merchant) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(categories, accounts) {
        if (selectedCategory == null) {
            val guessIcon = DefaultData.matchExpenseCategoryByKeyword(draft.rawText + " " + draft.merchant)
            val guess = categories.firstOrNull { it.icon == guessIcon } ?: categories.firstOrNull()
            selectedCategory = guess?.id
        }
        if (selectedAccount == null) {
            val match = accounts.firstOrNull { it.name == draft.accountHint }
                ?: accounts.firstOrNull { it.name.contains(draft.accountHint) }
                ?: accounts.firstOrNull()
            selectedAccount = match?.id
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) { },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SymbolText(
                            "bolt",
                            size = 16.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.autorecord_00),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${draft.accountHint} · 识别置信度 ${(draft.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = if (kind == "INCOME") stringResource(R.string.autorecord_01) else stringResource(R.string.autorecord_02),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                prefix = { Text("¥", style = MaterialTheme.typography.headlineLarge) },
                textStyle = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.autorecord_03),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { cat ->
                    CategoryPill(cat, cat.id == selectedCategory) { selectedCategory = cat.id }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.autorecord_04),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts, key = { it.id }) { acc ->
                    AccountPill(acc, acc.id == selectedAccount) { selectedAccount = acc.id }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.autorecord_05)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.autorecord_06)) }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val cents = amountText.toDoubleOrNull()?.let { Math.round(it * 100) } ?: 0L
                        val accId = selectedAccount ?: return@Button
                        if (cents <= 0L) return@Button
                        scope.launch {
                            repo.insertTransaction(
                                TransactionEntity(
                                    kind = kind,
                                    amount = cents,
                                    categoryId = selectedCategory,
                                    accountId = accId,
                                    note = note,
                                    timestamp = draft.timestamp,
                                    source = "AUTO",
                                    sourcePackage = draft.sourcePackage,
                                    sourceRef = draft.fingerprint
                                )
                            )
                            onSaved()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(stringResource(R.string.autorecord_07), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(cat: CategoryEntity, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SymbolText(
                cat.icon,
                size = 18.dp,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                cat.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountPill(acc: AccountEntity, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                acc.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
