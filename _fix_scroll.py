import io
p = r"app\src\main\java\io\github\cyhkbl\within\ui\assets\AssetsScreen.kt"
s = io.open(p, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {"""
new = """    val scrollState = rememberScrollState()
    LaunchedEffect(title) { scrollState.scrollTo(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
            ) {"""
if old in s:
    s = s.replace(old, new, 1); print("scroll OK")
else:
    print("scroll MISS")
io.open(p, "w", encoding="utf-8", newline="").write(s)
