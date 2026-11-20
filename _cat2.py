import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\category\CategoryManageActivity.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")

# CategoryRow 去掉子分类计数和「添加子类」
s = s.replace("""        Column(modifier = Modifier.weight(1f)) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge)
            if (childCount > 0) {
                Text(
                    "$childCount 个子分类",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        TextButton(onClick = onAddChild) { Text(stringResource(R.string.category_08)) }""",
"""        Text(
            category.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )""")

# CategoryRow 签名
s = s.replace("""    childCount: Int,
""", "")
s = s.replace("""    onAddChild: () -> Unit
""", "")

# CategoryEditorDialog 去掉 parentName
s = s.replace("""    parentName: String?,
""", "")
s = s.replace("""            Text(
                when {
                    initial?.parentId != null -> "新建子分类（父：$parentName）"
                    isNew -> stringResource(R.string.category_10)
                    else -> stringResource(R.string.category_11)
                }
            )""",
"""            Text(
                if (isNew) stringResource(R.string.category_10)
                else stringResource(R.string.category_11)
            )""")

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("category flattened")
