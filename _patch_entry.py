import io
path = r"app\src\main\java\io\github\cyhkbl\within\ui\entry\EntryScreen.kt"
raw = io.open(path, encoding="utf-8", newline="").read()
src = raw.replace("\r\n", "\n")

def sub(old, new, label):
    global src
    if old not in src:
        print("MISS:", label)
        return False
    src = src.replace(old, new, 1)
    print("OK:", label)
    return True

# 1) 参数：新增账户/时间回调
sub("""    onNote: (String) -> Unit,""",
    """    onNote: (String) -> Unit,
    onAccount: (Long) -> Unit,
    onTimestamp: (Long) -> Unit,""", "params")

# 2) 备注框限制三行，避免长备注把布局撑破
sub("""                BasicTextField(
                    value = state.note,
                    onValueChange = onNote,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )""",
    """                BasicTextField(
                    value = state.note,
                    onValueChange = onNote,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 76.dp)
                )""", "note maxLines")

# 3) 三个 chip 变成可点，并去掉没实现的标签 chip
sub("""            val account = state.accounts.firstOrNull { it.id == state.accountId }
            AssistChip(text = account?.name ?: stringResource(R.string.entry_04), icon = "account_balance_wallet")
            AssistChip(
                text = stringResource(R.string.entry_05) + DateUtils.formatTime(state.timestamp),
                icon = "schedule"
            )
            AssistChip(text = "", icon = "sell")""",
    """            val account = state.accounts.firstOrNull { it.id == state.accountId }
            AssistChip(
                text = account?.name ?: stringResource(R.string.entry_04),
                icon = "account_balance_wallet",
                onClick = { showAccountPicker = true }
            )
            AssistChip(
                text = DateUtils.formatDateTime(state.timestamp).substring(5),
                icon = "schedule",
                onClick = { showTimePicker = true }
            )""", "chips")

io.open(path, "w", encoding="utf-8", newline="").write(src)
