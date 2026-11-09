package io.github.cyhkbl.within.autolog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.cyhkbl.within.AutoRecordGate
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

class AutoLogSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                val testMerchant = stringResource(R.string.autolog_00)
                val testAccount = stringResource(R.string.autolog_01)
                val testRaw = stringResource(R.string.autolog_02)
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoLogSettingsScreen(
                        context = this,
                        onBack = { finish() },
                        onTest = {
                            AutoRecordGate.offer(
                                PaymentDraft(
                                    amount = 2880L,
                                    kind = "EXPENSE",
                                    merchant = testMerchant,
                                    accountHint = testAccount,
                                    sourcePackage = "com.tencent.mm",
                                    rawText = testRaw,
                                    timestamp = System.currentTimeMillis(),
                                    fingerprint = "test-" + System.currentTimeMillis()
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoLogSettingsScreen(
    context: Context,
    onBack: () -> Unit,
    onTest: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationGranted by remember { mutableStateOf(hasNotificationAccess(context)) }
    var accessibilityGranted by remember { mutableStateOf(hasAccessibilityAccess(context)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationGranted = hasNotificationAccess(context)
                accessibilityGranted = hasAccessibilityAccess(context)
                overlayGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.width(44.dp)
                ) {
                    OutlinedButton(onClick = onBack, border = null, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        SymbolText("arrow_back", size = 22.dp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.autolog_03), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.autolog_04),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.autolog_05),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.autolog_06),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Text(
                stringResource(R.string.autolog_07),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    PermissionRow(
                        icon = "notifications",
                        title = stringResource(R.string.autolog_08),
                        subtitle = if (notificationGranted) stringResource(R.string.autolog_09) else stringResource(R.string.autolog_10),
                        granted = notificationGranted,
                        actionLabel = if (notificationGranted) stringResource(R.string.autolog_11) else stringResource(R.string.autolog_12)
                    ) {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 74.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    PermissionRow(
                        icon = "accessibility",
                        title = stringResource(R.string.autolog_13),
                        subtitle = if (accessibilityGranted) stringResource(R.string.autolog_14) else stringResource(R.string.autolog_15),
                        granted = accessibilityGranted,
                        actionLabel = if (accessibilityGranted) stringResource(R.string.autolog_16) else stringResource(R.string.autolog_17)
                    ) {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 74.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    PermissionRow(
                        icon = "picture_in_picture",
                        title = stringResource(R.string.autolog_18),
                        subtitle = if (overlayGranted) stringResource(R.string.autolog_19) else stringResource(R.string.autolog_20),
                        granted = overlayGranted,
                        actionLabel = if (overlayGranted) stringResource(R.string.autolog_21) else stringResource(R.string.autolog_22)
                    ) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SymbolText("science", size = 22.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.autolog_23), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.autolog_24),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onTest,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(stringResource(R.string.autolog_25))
                    }
                }
            }
        }

        item {
            Text(
                stringResource(R.string.autolog_26),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun PermissionRow(
    icon: String,
    title: String,
    subtitle: String,
    granted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryGlyph(
            icon = icon,
            size = 42.dp,
            container = if (granted) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.secondaryContainer
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (granted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (granted) {
            SymbolText("check_circle", size = 24.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = onClick, shape = RoundedCornerShape(50)) {
                Text(actionLabel)
            }
        }
    }
}

private fun hasNotificationAccess(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false
    return enabled.contains(context.packageName)
}

private fun hasAccessibilityAccess(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabled.contains(context.packageName)
}
