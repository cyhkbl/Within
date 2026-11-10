package io.github.cyhkbl.within.ui.more

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.cyhkbl.within.autolog.AutoLogSettingsActivity
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.ui.category.CategoryManageActivity
import io.github.cyhkbl.within.ui.io.BackupRestoreActivity
import io.github.cyhkbl.within.ui.io.ImportExportActivity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.ThemePrefs
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import io.github.cyhkbl.within.R

private data class MoreItem(val icon: String, @StringRes val title: Int, @StringRes val subtitle: Int)

private val items = listOf(
    MoreItem("notifications_active", R.string.more_00, R.string.more_01),
    MoreItem("label", R.string.more_02, R.string.more_03),
    MoreItem("savings", R.string.more_04, R.string.more_05),
    MoreItem("event_repeat", R.string.more_06, R.string.more_07),
    MoreItem("file_upload", R.string.more_08, R.string.more_09),
    MoreItem("file_download", R.string.more_10, R.string.more_11),
    MoreItem("backup", R.string.more_12, R.string.more_13),
    MoreItem("palette", R.string.more_14, R.string.more_15),
    MoreItem("info", R.string.more_16, R.string.more_17)
)

@Composable
fun MoreScreen(repo: LedgerRepository) {
    val context = LocalContext.current
    var showAppearance by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showComingSoon by remember { mutableStateOf<Int?>(null) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(stringResource(R.string.more_18), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.more_19),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (item.title) {
                                        R.string.more_00 -> context.startActivity(
                                            android.content.Intent(context, AutoLogSettingsActivity::class.java)
                                        )
                                        R.string.more_02 -> context.startActivity(
                                            android.content.Intent(context, CategoryManageActivity::class.java)
                                        )
                                        R.string.more_08, R.string.more_10 -> context.startActivity(
                                            android.content.Intent(context, ImportExportActivity::class.java)
                                        )
                                        R.string.more_12 -> context.startActivity(
                                            android.content.Intent(context, BackupRestoreActivity::class.java)
                                        )
                                        R.string.more_14 -> showAppearance = true
                                        R.string.more_16 -> showAbout = true
                                        else -> showComingSoon = item.title
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryGlyph(icon = item.icon, size = 42.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(item.title), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(item.subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            SymbolText(
                                "chevron_right",
                                size = 20.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index != items.lastIndex) {
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

    if (showAppearance) {
        AppearanceDialog(onDismiss = { showAppearance = false })
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.more_27)) },
            text = {
                Column {
                    Text(stringResource(R.string.more_28), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Material 3 Expressive · Jetpack Compose 1.12 · AGP 9.5.1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.more_29),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.more_30)) }
            }
        )
    }

    showComingSoon?.let { titleRes ->
        val titleText = stringResource(titleRes)
        AlertDialog(
            onDismissRequest = { showComingSoon = null },
            title = { Text(titleText) },
            text = { Text(stringResource(R.string.more_coming_soon, titleText)) },
            confirmButton = {
                TextButton(onClick = { showComingSoon = null }) { Text(stringResource(R.string.more_31)) }
            }
        )
    }
}

@Composable
private fun AppearanceDialog(onDismiss: () -> Unit) {
    val mode by ThemePrefs.mode.collectAsState()
    val dynamic by ThemePrefs.dynamicColor.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.more_32)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.more_33),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                listOf(
                    ThemePrefs.MODE_SYSTEM to stringResource(R.string.more_34),
                    ThemePrefs.MODE_LIGHT to stringResource(R.string.more_35),
                    ThemePrefs.MODE_DARK to stringResource(R.string.more_36)
                ).forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ThemePrefs.setMode(value) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = mode == value, onClick = { ThemePrefs.setMode(value) })
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.more_37), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.more_38),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = dynamic, onCheckedChange = { ThemePrefs.setDynamicColor(it) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.more_39)) }
        }
    )
}
