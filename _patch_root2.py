import io
path = r"app\src\main\java\io\github\cyhkbl\within\ui\LedgerRoot.kt"
src = io.open(path, encoding="utf-8", newline="").read().replace("\r\n", "\n")
old = """                onNote = vm::setNote,
                onSave = { vm.save { nav.popBackStack() } },"""
new = """                onNote = vm::setNote,
                onAccount = vm::selectAccount,
                onTimestamp = vm::setTimestamp,
                onSave = { vm.save { nav.popBackStack() } },"""
if old in src:
    src = src.replace(old, new, 1); print("OK wiring")
else:
    print("MISS wiring")
io.open(path, "w", encoding="utf-8", newline="").write(src)
