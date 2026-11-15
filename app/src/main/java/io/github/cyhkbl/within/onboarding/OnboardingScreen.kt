package io.github.cyhkbl.within.onboarding

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyhkbl.within.ui.components.SymbolText
import kotlinx.coroutines.delay

private data class PermissionItem(
    val key: String,
    val icon: String,
    val title: String,
    val description: String,
    val check: (android.content.Context) -> Boolean,
    val open: (android.content.Context) -> Unit,
    val optional: Boolean = false
)

/**
 * 首次启动的引导页。
 *
 * 自动记账要靠一串系统授权才能跑起来，缺一个都不行，而且 Android 不会主动提示。
 * 之前网页支付完什么都不弹，就是因为无障碍和通知监听压根没开——用户没地方知道这事。
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    var tick by remember { mutableStateOf(0) }

    // 从系统设置页回来时状态会变，这里轮询刷新
    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            tick++
        }
    }

    val items = remember {
        listOf(
            PermissionItem(
                key = "a11y",
                icon = "touch_app",
                title = "无障碍服务",
                description = "识别微信、支付宝、银行 App 的支付完成页面，这是自动记账最主要的来源。",
                check = { AutoRecordPermissions.accessibilityEnabled(it) },
                open = { AutoRecordPermissions.openAccessibilitySettings(it) }
            ),
            PermissionItem(
                key = "notif_listener",
                icon = "notifications_active",
                title = "通知使用权",
                description = "读取支付通知，即使无障碍没识别到，也能从通知里拿到金额和商户。",
                check = { AutoRecordPermissions.notificationListenerEnabled(it) },
                open = { AutoRecordPermissions.openNotificationListenerSettings(it) }
            ),
            PermissionItem(
                key = "battery",
                icon = "battery_saver",
                title = "允许后台运行",
                description = "加入电池优化白名单。不加的话系统会在息屏后把应用冻结，支付时收不到任何触发。",
                check = { AutoRecordPermissions.batteryOptimizationIgnored(it) },
                open = { AutoRecordPermissions.requestIgnoreBatteryOptimizations(it) }
            ),
            PermissionItem(
                key = "notification",
                icon = "notifications",
                title = "通知权限",
                description = "检测到支付后，用高优先级通知把记账卡片推到你面前。",
                check = { AutoRecordPermissions.notificationsGranted(it) },
                open = { AutoRecordPermissions.openAppNotificationSettings(it) }
            ),
            PermissionItem(
                key = "overlay",
                icon = "picture_in_picture_alt",
                title = "悬浮窗（可选）",
                description = "Android 10 之后禁止后台直接弹界面。有悬浮窗权限时，记账卡片能直接浮在支付页上。",
                check = { AutoRecordPermissions.canDrawOverlays(it) },
                open = { AutoRecordPermissions.openOverlaySettings(it) },
                optional = true
            ),
            PermissionItem(
                key = "autostart",
                icon = "rocket_launch",
                title = "允许自启动",
                description = "一加、OPPO 默认禁止应用自启动。不开的话，重启手机或闲置一段时间后本应用就再也不会被唤起，记账也就断了。",
                check = { AutoRecordPermissions.autoStartLikelyAllowed(it) },
                open = { AutoRecordPermissions.openAutoStartSettings(it) }
            )
        )
    }

    val checks = items.associateWith { it.check(context) }
    val pendingRequired = items.filter { !it.optional && checks[it] != true }
    val pendingOptional = items.filter { it.optional && checks[it] != true }
    val doneItems = items.filter { checks[it] == true }
    val required = items.filter { !it.optional }
    val requiredDone = required.count { checks[it] == true }
    val allDone = pendingRequired.isEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                if (allDone) "一切就绪" else "让记账自动发生",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (allDone) {
                    "该开的都开了。下次在微信、支付宝或银行 App 里付完款，记账卡片会自己跳出来。"
                } else {
                    "还差 ${pendingRequired.size} 项授权，才能在支付完成的瞬间把记账卡片递给你。"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(modifier = Modifier.height(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(requiredDone.toFloat() / required.size)
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // 还没开的必需项：直接列出来
            pendingRequired.forEach { item ->
                PermissionCard(item = item, granted = false, onClick = { item.open(context) })
                Spacer(Modifier.height(12.dp))
            }

            // 已完成的收成一行，不再占版面
            if (doneItems.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SymbolText(
                            "check_circle",
                            size = 18.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "已完成 ${doneItems.size} 项：" + doneItems.joinToString("、") { it.title },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 可选项只在没开时提示
            if (pendingOptional.isNotEmpty() && allDone) {
                Text(
                    "想让弹窗更稳，可以再开两项",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                pendingOptional.forEach { item ->
                    PermissionCard(item = item, granted = false, onClick = { item.open(context) })
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    OnboardingPrefs.markDone(context)
                    onFinish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    if (allDone) "开始使用" else "稍后再说",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (allDone) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        SymbolText(
                            "push_pin",
                            size = 18.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "最后一件事：打开最近任务，把「量入为出」的卡片往下拉（或长按）锁定住。这样系统清理后台时不会把它一起划掉。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Text(
                "没授的权限可以随时在「我的 → 自动记账」里补。",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItem,
    granted: Boolean,
    onClick: () -> Unit
) {
    val container = if (granted) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val accent = if (granted) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = container
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                SymbolText(item.icon, size = 22.dp, color = accent)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(granted = granted, optional = item.optional)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(granted: Boolean, optional: Boolean) {
    val (text, container, content) = when {
        granted -> Triple(
            "已开启",
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        optional -> Triple(
            "可选",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Triple(
            "去开启",
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.primary
        )
    }
    Surface(shape = CircleShape, color = container) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = content
        )
    }
}
