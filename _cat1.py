import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\category\CategoryManageActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

# 1) 列表只渲染一级，去掉子分类与「加子类」回调
old_list = """                items(topLevel, key = { it.id }) { cat ->
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
                }"""
new_list = """                items(list, key = { it.id }) { cat ->
                    CategoryRow(
                        category = cat,
                        onEdit = { editing = cat; showDialog = true },
                        onDelete = { pendingDelete = cat }
                    )
                }"""
if old_list in s:
    s = s.replace(old_list, new_list, 1); print("list OK")
else:
    print("MISS list")

# 2) 新建时不再带 parentId
s = s.replace("""            parentName = editing?.parentId?.let { pid -> list.firstOrNull { it.id == pid }?.name },
""", "")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("step1 done")
