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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.io.ImportResult
import io.github.cyhkbl.within.io.LedgerCsv
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

class ImportExportActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImportExportScreen(
                        context = this,
                        repo = repo,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportExportScreen(
    context: Context,
    repo: LedgerRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var pendingImport by remember { mutableStateOf<List<io.github.cyhkbl.within.io.ImportRow>?>(null) }
    var lastResult by remember { mutableStateOf<ImportResult?>(null) }
    var busy by remember { mutableStateOf(false) }

    // 这些文案要放进协程/回调里用，必须在 Composable 作用域先取出来
    val exportPrefix = stringResource(R.string.io_export_prefix)
    val errUnknown = stringResource(R.string.common_error_unknown)
    val statusExportFailed = stringResource(R.string.io_status_export_failed)
    val statusParsedEmpty = stringResource(R.string.io_status_parsed_empty)
    val statusParseFailed = stringResource(R.string.io_status_read_failed)
    val statusCancelled = stringResource(R.string.io_22)

    val exportNow: () -> Unit = {
        busy = true
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
                    val requestedName = exportPrefix + LocalDate.now() + ".csv"
                    // 用实际落盘的文件名：下载目录已有同名文件时系统会追加 (1)(2)
                    val actualName = writeCsvToDownloads(context, requestedName, csv)
                    actualName to txs.size
                }
                status = context.getString(R.string.io_status_exported, result.second, result.first)
            } catch (t: Throwable) {
                status = statusExportFailed + (t.message ?: errUnknown)
            } finally {
                busy = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        scope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    val text = context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    } ?: ""
                    LedgerCsv.parseCsv(text)
                }
                pendingImport = rows
                status = if (rows.isEmpty()) statusParsedEmpty
                else context.getString(R.string.io_status_parsed, rows.size)
            } catch (t: Throwable) {
                status = statusParseFailed + (t.message ?: errUnknown)
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
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
                    Text(stringResource(R.string.io_06))
                }
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.io_07), style = MaterialTheme.typography.titleMedium)
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    stringResource(R.string.io_08),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.io_09),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        ActionCard(
            icon = "file_download",
            title = stringResource(R.string.io_10),
            subtitle = stringResource(R.string.io_11)
        ) {
            Button(
                onClick = { exportNow() },
                enabled = !busy,
                shape = RoundedCornerShape(50)
            ) { Text(stringResource(R.string.io_12)) }
        }

        Spacer(Modifier.height(12.dp))

        ActionCard(
            icon = "file_upload",
            title = stringResource(R.string.io_13),
            subtitle = stringResource(R.string.io_14)
        ) {
            Button(
                // 不用 MIME 过滤：DocumentsUI 对 text/* 这类通配不生效，
                // 会把刚导出的 .csv 直接过滤掉，导致用户找不到文件。
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                enabled = !busy,
                shape = RoundedCornerShape(50)
            ) { Text(stringResource(R.string.io_15)) }
        }

        val pending = pendingImport
        if (pending != null && pending.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("待导入 ${pending.size} 条", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    val preview = pending.take(3)
                    preview.forEach { row ->
                        val kindLabel = when (row.kind) {
                            "INCOME" -> stringResource(R.string.io_16)
                            "TRANSFER" -> stringResource(R.string.io_17)
                            else -> stringResource(R.string.io_18)
                        }
                        Text(
                            "${row.clock.toLocalDate()} · " + kindLabel + " · " +
                                io.github.cyhkbl.within.util.MoneyUtils.format(row.amountCents) +
                                " · " + row.categoryName.ifBlank { stringResource(R.string.io_19) } +
                                " · " + row.accountName.ifBlank { stringResource(R.string.io_20) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (pending.size > 3) {
                        Text(
                            "…… 还有 ${pending.size - 3} 条",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row {
                        Button(
                            onClick = {
                                busy = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        LedgerCsv.importInto(repo, pending)
                                    }
                                    lastResult = result
                                    pendingImport = null
                                    status = "导入完成：成功 ${result.imported} 条，跳过 ${result.skipped} 条"
                                    busy = false
                                }
                            },
                            enabled = !busy,
                            shape = RoundedCornerShape(50)
                        ) { Text(stringResource(R.string.io_21)) }
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { pendingImport = null; status = statusCancelled },
                            shape = RoundedCornerShape(50)
                        ) { Text(stringResource(R.string.io_23)) }
                    }
                }
            }
        }

        if (status.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        lastResult?.let { result ->
            if (result.messages.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                result.messages.forEach { msg ->
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun ActionCard(
    icon: String,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryGlyph(icon = icon, size = 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            action()
        }
    }
}

/**
 * 写到「下载」目录。
 *
 * 之前用 SAF CreateDocument 存文件，文件由 DocumentsProvider 以 root 身份落盘，
 * MediaStore 不索引，结果在系统的文件选择器里根本看不到刚导出的 CSV。
 * 改用 MediaStore.Downloads 写入，文件归属本应用且立即出现在「下载」里。
 */
private fun writeCsvToDownloads(context: Context, name: String, csv: String): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        throw IllegalStateException(context.getString(R.string.io_24))
    }
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, name)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }
    val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    val uri = context.contentResolver.insert(collection, values)
        ?: throw IllegalStateException(context.getString(R.string.io_25))
    context.contentResolver.openOutputStream(uri)?.use { out ->
        out.write(csv.toByteArray(Charsets.UTF_8))
    } ?: throw IllegalStateException(context.getString(R.string.io_26))

    // 同名文件已存在时系统会改名，回读真实文件名，避免提示与实际不符
    var actualName = name
    context.contentResolver.query(
        uri,
        arrayOf(MediaStore.Downloads.DISPLAY_NAME),
        null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            actualName = cursor.getString(0) ?: name
        }
    }
    return actualName
}
