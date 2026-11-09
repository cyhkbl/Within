package io.github.cyhkbl.within.ui.category

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.ui.components.CategoryGlyph
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import io.github.cyhkbl.within.R

/** 分类图标候选，名字都是 Material Symbols 的 ligature。 */
private val iconChoices = listOf(
    "restaurant", "cookie", "checkroom", "directions_bus", "flight", "child",
    "pets", "call", "wine_bar", "menu_book", "shopping_basket", "home",
    "face_retouching_natural", "local_hospital", "redeem", "directions_car",
    "sports_esports", "celebration", "devices", "fitness_center", "bolt",
    "category", "payments", "card_giftcard", "savings", "trending_up",
    "work", "add_business", "school", "coffee"
)

class CategoryManageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as LedgerApp).repo
        setContent {
            LedgerTheme {
                CategoryManageScreen(repo) { finish() }
            }
        }
    }
}

@Composable
private fun CategoryManageScreen(repo: LedgerRepository, onBack: () -> Unit) {
    var kind by remember { mutableStateOf("EXPENSE") }
    val all by repo.allCategories().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    val list = all.filter { it.kind == kind }.sortedBy { it.sortOrder }
    val topLevel = list.filter { it.parentId == null }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SymbolText("add", size = 26.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
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
                        Text(stringResource(R.string.category_00))
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.category_01), style = MaterialTheme.typography.titleMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KindChip(stringResource(R.string.category_02), kind == "EXPENSE") { kind = "EXPENSE" }
                KindChip(stringResource(R.string.category_03), kind == "INCOME") { kind = "INCOME" }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(topLevel, key = { it.id }) { cat ->
                    CategoryRow(
                        category = cat,
                        childCount = list.count { it.parentId == cat.id },
                        onEdit = { editing = cat; showDialog = true },
                        onDelete = { pendingDelete = cat },
                        onAddChild = {
                            editing = CategoryEntity(
                                name = "",
                                kind = kind,
                                parentId = cat.id,
                                icon = cat.icon,
                                sortOrder = list.size
                            )
                            showDialog = true
                        }
                    )
                    list.filter { it.parentId == cat.id }.forEach { child ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editing = child; showDialog = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SymbolText(
                                    child.icon,
                                    size = 18.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(child.name, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { pendingDelete = child }) {
                                    Text(stringResource(R.string.category_04), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    if (showDialog) {
        CategoryEditorDialog(
            initial = editing,
            kind = kind,
            parentName = editing?.parentId?.let { pid -> list.firstOrNull { it.id == pid }?.name },
            onDismiss = { showDialog = false },
            onSave = { entity ->
                showDialog = false
                scope.launch { repo.saveCategory(entity) }
            }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.category_05)) },
            text = {
                Text("确定删除「${target.name}」？已经用它记过的账单不会消失，但会变成未分类。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = null
                        scope.launch {
                            repo.saveCategory(target.copy(archived = true))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.category_06)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.category_07)) }
            }
        )
    }
}

@Composable
private fun KindChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    childCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddChild: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onEdit)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryGlyph(icon = category.icon, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge)
            if (childCount > 0) {
                Text(
                    "$childCount 个子分类",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        TextButton(onClick = onAddChild) { Text(stringResource(R.string.category_08)) }
        TextButton(onClick = onDelete) {
            Text(stringResource(R.string.category_09), color = MaterialTheme.colorScheme.error)
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 58.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun CategoryEditorDialog(
    initial: CategoryEntity?,
    kind: String,
    parentName: String?,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "category") }
    val isNew = initial == null || initial.id == 0L
    val fallbackName = stringResource(R.string.category_14)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    initial?.parentId != null -> "新建子分类（父：$parentName）"
                    isNew -> stringResource(R.string.category_10)
                    else -> stringResource(R.string.category_11)
                }
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_12)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.category_13),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconChoices) { choice ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (choice == icon) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable { icon = choice },
                            contentAlignment = Alignment.Center
                        ) {
                            SymbolText(
                                choice,
                                size = 22.dp,
                                color = if (choice == icon) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val base = initial ?: CategoryEntity(name = name, kind = kind, icon = icon)
                    onSave(
                        base.copy(
                            name = name.trim().ifBlank { fallbackName },
                            icon = icon,
                            kind = kind
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.category_15)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.category_16)) } }
    )
}
