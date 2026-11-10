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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.data.db.LedgerDatabase
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BackupRestoreActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LedgerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BackupRestoreScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
private fun BackupRestoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    var confirmWipe by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> if (uri != null) pendingRestore = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
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
            Text("备份与恢复", style = MaterialTheme.typography.titleMedium)
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "整份数据库备份",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "备份文件包含全部账单、账户、分类和设置。恢复会覆盖当前所有数据，操作前请先备份一次。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryGlyph(icon = "backup", size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("备份到下载目录", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "生成一个 .db 文件，可以拷到别处保管",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        busy = true
                        status = ""
                        scope.launch {
                            val result = withContext(Dispatchers.IO) { backupNow(context) }
                            status = result
                            busy = false
                        }
                    },
                    enabled = !busy,
                    shape = RoundedCornerShape(50)
                ) { Text(if (busy) "处理中…" else "立即备份") }
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryGlyph(icon = "restore", size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("从备份恢复", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "选择之前备份的 .db 文件，会覆盖当前数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { restoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) },
                    enabled = !busy,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("选择备份文件") }
            }
        }

        if (status.isNotBlank()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(status, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "清空全部账单",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "删掉所有账单并把账户余额归零，账户和分类保留。换账本、或者重新导入前用这个。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { confirmWipe = true },
                    enabled = !busy,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) { Text("清空账单") }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (confirmWipe) {
        AlertDialog(
            onDismissRequest = { confirmWipe = false },
            title = { Text("确认清空？") },
            text = { Text("所有账单会被删除，账户余额归零，无法撤回。建议先做一次备份。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmWipe = false
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                val repo = (context.applicationContext as LedgerApp).repo
                                repo.clearTransactionsAndBalances()
                                "已清空全部账单"
                            }.getOrElse { "清空失败：" + (it.message ?: "未知错误") }
                        }
                        status = result
                        busy = false
                    }
                }) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { confirmWipe = false }) { Text("取消") }
            }
        )
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("确认恢复？") },
            text = { Text("这会用备份文件覆盖当前全部账单和账户，当前数据无法撤回。") },
            confirmButton = {
                TextButton(onClick = {
                    val target = uri
                    pendingRestore = null
                    busy = true
                    status = ""
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { restoreNow(context, target) }
                        status = result
                        busy = false
                    }
                }) { Text("覆盖恢复") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("取消") }
            }
        )
    }
}

private fun backupNow(context: Context): String {
    return try {
        val db = LedgerDatabase.get(context)
        // 把 WAL 里的改动落盘，否则备份出来的可能是旧数据
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        db.close()

        val source = context.getDatabasePath("ledger.db")
        if (!source.exists()) return "找不到数据库文件，先记一笔再试"

        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val name = "within-backup-$stamp.db"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return "无法在下载目录创建文件"
            context.contentResolver.openOutputStream(uri)?.use { out ->
                source.inputStream().use { it.copyTo(out) }
            } ?: return "无法写入备份文件"
            "已备份到「下载/$name」"
        } else {
            val target = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                name
            )
            source.inputStream().use { input ->
                target.outputStream().use { input.copyTo(it) }
            }
            "已备份到「下载/$name」"
        }
    } catch (t: Throwable) {
        "备份失败：" + (t.message ?: "未知错误")
    }
}

private fun restoreNow(context: Context, uri: Uri): String {
    return try {
        val db = LedgerDatabase.get(context)
        db.close()

        val target = context.getDatabasePath("ledger.db")
        target.parentFile?.mkdirs()

        // 先清掉旧的 WAL/SHM，避免和恢复进来的库对不上
        File(target.path + "-wal").delete()
        File(target.path + "-shm").delete()

        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { input.copyTo(it) }
        } ?: return "读取备份文件失败"

        "恢复完成，请完全退出应用再重新打开"
    } catch (t: Throwable) {
        "恢复失败：" + (t.message ?: "未知错误")
    }
}
