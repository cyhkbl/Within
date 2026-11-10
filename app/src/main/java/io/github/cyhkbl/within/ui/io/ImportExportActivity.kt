package io.github.cyhkbl.within.ui.io

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.R
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.io.ImportFormat
import io.github.cyhkbl.within.io.ImportPreview
import io.github.cyhkbl.within.io.ImportRunner
import io.github.cyhkbl.within.io.ImportSummary
import io.github.cyhkbl.within.io.LedgerCsv
import io.github.cyhkbl.within.io.LedgerImport
import io.github.cyhkbl.within.io.ParseOutcome
import io.github.cyhkbl.within.io.ParsedRow
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import io.github.cyhkbl.within.util.MoneyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ImportExportActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ImportExportScreen(context = this, repo = repo, onBack = { finish() })
                }
            }
        }
    }
}

private sealed interface Stage {
    data object Idle : Stage
    data object Working : Stage
    data class Preview(val outcome: ParseOutcome, val preview: ImportPreview) : Stage
    data class Done(val summary: ImportSummary) : Stage
}

@Composable
private fun ImportExportScreen(
    context: Context,
    repo: LedgerRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val runner = remember { ImportRunner(repo) }
    var stage by remember { mutableStateOf<Stage>(Stage.Idle) }
    var message by remember { mutableStateOf("") }
    var exporting by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val exportPrefix = androidx.compose.ui.res.stringResource(R.string.io_export_prefix)
    val errUnknown = androidx.compose.ui.res.stringResource(R.string.common_error_unknown)

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        stage = Stage.Working
        message = ""
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val name = queryName(context, uri) ?: "import.csv"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: ByteArray(0)
                    val outcome = LedgerImport.parse(bytes, name)
                    val preview = runner.preview(outcome)
                    outcome to preview
                }
                val (outcome, preview) = result
                if (outcome.rows.isEmpty() && outcome.issues.isNotEmpty()) {
                    stage = Stage.Idle
                    message = outcome.issues.first().reason
                } else {
                    stage = Stage.Preview(outcome, preview)
                }
            } catch (t: Throwable) {
                stage = Stage.Idle
                message = "读取失败：" + (t.message ?: errUnknown)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? -> if (uri != null) { /* 由 exportNow 处理 */ } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        border = null,
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        SymbolText("arrow_back", size = 20.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("返回")
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("导入导出", style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "直接读取钱迹的导出文件",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "支持钱迹导出的 XLSX 和 CSV（19 列原名表头也能读），也支持其它记账应用导出的 CSV。按表头名字识别，不依赖列顺序。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // ── 导入 ──
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryGlyph(icon = "file_upload", size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("导入账单", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "XLSX / CSV，按表头自动识别",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            importLauncher.launch(
                                arrayOf(
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "text/plain",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel",
                                    "application/octet-stream"
                                )
                            )
                        },
                        enabled = stage !is Stage.Working,
                        shape = RoundedCornerShape(50)
                    ) { Text("选择文件") }
                }
            }
        }

        when (val s = stage) {
            is Stage.Working -> item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("正在解析…", style = MaterialTheme.typography.bodyMedium)
                }
            }

            is Stage.Preview -> {
                item { PreviewSummaryCard(s) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                stage = Stage.Working
                                progress = 0f
                                scope.launch {
                                    val summary = withContext(Dispatchers.IO) {
                                        runner.run(s.outcome) { done, total ->
                                            progress = if (total <= 0) 1f else done.toFloat() / total
                                        }
                                    }
                                    stage = Stage.Done(summary)
                                }
                            },
                            shape = RoundedCornerShape(50)
                        ) { Text("确认导入") }
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { stage = Stage.Idle },
                            shape = RoundedCornerShape(50)
                        ) { Text("取消") }
                    }
                }

                if (s.preview.issues.isNotEmpty()) {
                    item {
                        Text(
                            "解析失败的行",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    itemsIndexed(s.preview.issues) { _, issue ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "第 ${issue.lineNo} 行 · ${issue.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "将导入的账单（${s.preview.newRows.size} 条）",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                items(s.preview.newRows.take(60)) { row -> ParsedRowItem(row) }
                if (s.preview.newRows.size > 60) {
                    item {
                        Text(
                            "…… 还有 ${s.preview.newRows.size - 60} 条，确认后会全部导入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is Stage.Done -> {
                item { ImportResultCard(s.summary) }
                item {
                    OutlinedButton(
                        onClick = {
                            val summary = s.summary
                            stage = Stage.Working
                            scope.launch {
                                val removed = withContext(Dispatchers.IO) { runner.undo(summary.batchId) }
                                stage = Stage.Idle
                                message = "已撤销这次导入，删除 $removed 条账单"
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("撤销这次导入") }
                }
            }

            Stage.Idle -> Unit
        }

        if (message.isNotBlank()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // ── 导出 ──
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryGlyph(icon = "file_download", size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("导出账单", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "保存 CSV 到「下载」目录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            exporting = true
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        val txs = repo.recentTransactionsOnce(100_000)
                                        val cats = repo.allCategoriesOnce()
                                        val accounts = repo.accountsAllOnce()
                                        val csv = LedgerCsv.buildCsv(
                                            txs,
                                            cats.associate { it.id to it.name },
                                            accounts.associate { it.id to it.name }
                                        )
                                        val requested = exportPrefix + LocalDate.now() + ".csv"
                                        val actual = writeCsvToDownloads(context, requested, csv)
                                        actual to txs.size
                                    }
                                    message = context.getString(
                                        R.string.io_status_exported, result.second, result.first
                                    )
                                } catch (t: Throwable) {
                                    message = "导出失败：" + (t.message ?: errUnknown)
                                } finally {
                                    exporting = false
                                }
                            }
                        },
                        enabled = !exporting,
                        shape = RoundedCornerShape(50)
                    ) { Text(if (exporting) "导出中…" else "导出到下载目录") }
                }
            }
        }
    }

    if (stage is Stage.Working && progress > 0f) {
        Box(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PreviewSummaryCard(preview: Stage.Preview) {
    val p = preview.preview
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "解析完成 · " + when (p.format) {
                    ImportFormat.XLSX -> "XLSX"
                    ImportFormat.CSV -> "CSV"
                    ImportFormat.UNKNOWN -> "未知格式"
                },
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            Row {
                StatBlock("可导入", "${p.newRows.size}")
                StatBlock("重复跳过", "${p.duplicateCount}")
                StatBlock("失败", "${p.issues.size}")
            }
            if (p.duplicateCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "检测到 ${p.duplicateCount} 条与已有账单重复（时间、金额、账户、类型都相同），已自动跳过。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImportResultCard(summary: ImportSummary) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "导入完成",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "成功 ${summary.imported} 条，重复跳过 ${summary.skippedDuplicate} 条，失败 ${summary.failed} 条",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(modifier = Modifier.padding(end = 24.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ParsedRowItem(row: ParsedRow) {
    val kindLabel = when (row.kind) {
        "INCOME" -> "收入"
        "TRANSFER" -> "转账"
        "REFUND" -> "退款"
        else -> "支出"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(kindLabel.take(1), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                row.category.ifBlank { "未分类" } + " · " + row.note.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                "${row.clock.toLocalDate()} · $kindLabel · ${row.account}" +
                    if (row.toAccount.isNotBlank()) " → ${row.toAccount}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            MoneyUtils.format(row.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun queryName(context: Context, uri: Uri): String? {
    context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }
    return uri.lastPathSegment
}

private fun writeCsvToDownloads(context: Context, name: String, csv: String): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        throw IllegalStateException("系统版本过低，暂不支持自动导出")
    }
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, name)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }
    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        ?: throw IllegalStateException("无法在下载目录创建文件")
    context.contentResolver.openOutputStream(uri)?.use { out ->
        out.write(csv.toByteArray(Charsets.UTF_8))
    } ?: throw IllegalStateException("无法写入文件")

    var actualName = name
    context.contentResolver.query(
        uri, arrayOf(MediaStore.Downloads.DISPLAY_NAME), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) actualName = cursor.getString(0) ?: name
    }
    return actualName
}
