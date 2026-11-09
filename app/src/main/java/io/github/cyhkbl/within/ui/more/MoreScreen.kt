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
import io.github.cyhkbl.within.ui.io.ImportExportActivity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.ThemePrefs

private data class MoreItem(val icon: String, val title: String, val subtitle: String)

private val items = listOf(
    MoreItem("notifications_active", "自动记账", "支付完成时自动弹出记账"),
    MoreItem("label", "分类管理", "增删改一二级分类"),
    MoreItem("savings", "预算", "月度与分类预算"),
    MoreItem("event_repeat", "周期记账", "房租、订阅等自动生成"),
    MoreItem("file_upload", "导入账单", "支付宝、微信账单导入"),
    MoreItem("file_download", "导出账单", "导出 CSV / Excel"),
    MoreItem("backup", "备份与恢复", "本地备份"),
    MoreItem("palette", "外观", "主题、深色模式、动态取色"),
    MoreItem("info", "关于", "版本与开源许可")
)

@Composable
fun MoreScreen(repo: LedgerRepository) {
    val context = LocalContext.current
    var showAppearance by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text("我的", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "账本设置与数据管理",
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
                                        "自动记账" -> context.startActivity(
                                            android.content.Intent(context, AutoLogSettingsActivity::class.java)
                                        )
                                        "分类管理" -> context.startActivity(
                                            android.content.Intent(context, CategoryManageActivity::class.java)
                                        )
                                        "导入账单", "导出账单", "备份与恢复" -> context.startActivity(
                                            android.content.Intent(context, ImportExportActivity::class.java)
                                        )
                                        "外观" -> showAppearance = true
                                        "关于" -> showAbout = true
                                        else -> showComingSoon = item.title
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryGlyph(icon = item.icon, size = 42.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    item.subtitle,
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
            title = { Text("关于") },
            text = {
                Column {
                    Text("账本 v0.2.0", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Material 3 Expressive · Jetpack Compose 1.12 · AGP 9.5.1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "数据全部存在本机，没有账号、没有云同步、没有广告。自动记账读的是系统的支付通知和支付结果页，内容不会被上传。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("知道了") }
            }
        )
    }

    showComingSoon?.let { title ->
        AlertDialog(
            onDismissRequest = { showComingSoon = null },
            title = { Text(title) },
            text = { Text("「$title」还在开发中，下一个版本会补上。") },
            confirmButton = {
                TextButton(onClick = { showComingSoon = null }) { Text("好") }
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
        title = { Text("外观") },
        text = {
            Column {
                Text(
                    "主题",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                listOf(
                    ThemePrefs.MODE_SYSTEM to "跟随系统",
                    ThemePrefs.MODE_LIGHT to "浅色",
                    ThemePrefs.MODE_DARK to "深色"
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
                        Text("动态取色", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "用系统壁纸的颜色生成主题色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = dynamic, onCheckedChange = { ThemePrefs.setDynamicColor(it) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        }
    )
}
