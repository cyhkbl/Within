import io
path = r"app\src\main\java\io\github\cyhkbl\within\ui\entry\EntryScreen.kt"
raw = io.open(path, encoding="utf-8", newline="").read()
src = raw.replace("\r\n", "\n")

def sub(old, new, label):
    global src
    if old not in src:
        print("MISS:", label); return
    src = src.replace(old, new, 1); print("OK:", label)

# 1) AssistChip 支持点击
sub("private fun AssistChip(text: String, icon: String) {",
    "private fun AssistChip(text: String, icon: String, onClick: () -> Unit) {", "chip sig")
sub("""        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.clip(RoundedCornerShape(50))
    ) {""",
    """        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {""", "chip clickable")

# 2) 状态变量
sub("""    val amountText = if (state.amountInput.isEmpty()) "0.00" else state.amountInput
""",
    """    var showAccountPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val amountText = if (state.amountInput.isEmpty()) "0.00" else state.amountInput
""", "state vars")

# 3) 在 Column 末尾插入两个对话框
sub("""                    Text(stringResource(R.string.entry_06), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}""",
    """                    Text(stringResource(R.string.entry_06), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showAccountPicker) {
            AlertDialog(
                onDismissRequest = { showAccountPicker = false },
                title = { Text(stringResource(R.string.entry_04)) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        state.accounts.forEach { acc ->
                            TextButton(
                                onClick = {
                                    onAccount(acc.id)
                                    showAccountPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(acc.name, modifier = Modifier.weight(1f))
                                Text(MoneyUtils.format(acc.balance))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAccountPicker = false }) { Text(stringResource(R.string.common_cancel)) }
                }
            )
        }

        if (showTimePicker) {
            val dateState = rememberDatePickerState(initialSelectedDateMillis = state.timestamp)
            @OptIn(ExperimentalMaterial3Api::class)
            DatePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            val zone = java.time.ZoneId.systemDefault()
                            val old = DateUtils.toLocalDateTime(state.timestamp)
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(zone).toLocalDate()
                            onTimestamp(date.atTime(old.toLocalTime()).atZone(zone).toInstant().toEpochMilli())
                        }
                        showTimePicker = false
                    }) { Text(stringResource(R.string.common_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.common_cancel)) }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
    }
}""", "dialogs")

# 4) imports
needed = [
    "import androidx.compose.foundation.layout.heightIn",
    "import androidx.compose.foundation.layout.verticalScroll",
    "import androidx.compose.foundation.rememberScrollState",
    "import androidx.compose.material3.AlertDialog",
    "import androidx.compose.material3.DatePicker",
    "import androidx.compose.material3.DatePickerDialog",
    "import androidx.compose.material3.ExperimentalMaterial3Api",
    "import androidx.compose.material3.TextButton",
    "import androidx.compose.material3.rememberDatePickerState",
    "import androidx.compose.runtime.getValue",
    "import androidx.compose.runtime.mutableStateOf",
    "import androidx.compose.runtime.remember",
    "import androidx.compose.runtime.setValue",
    "import io.github.cyhkbl.within.util.MoneyUtils",
]
anchor = "import androidx.compose.runtime.Composable"
add = "\n".join(x for x in needed if x not in src)
if add:
    src = src.replace(anchor, anchor + "\n" + add, 1)
    print("imports added:", len(add.split("\n")))

io.open(path, "w", encoding="utf-8", newline="").write(src)
