package io.github.cyhkbl.within.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.util.MoneyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryUiState(
    val kind: String = "EXPENSE",
    val amountInput: String = "",
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val editingId: Long? = null,
    val savedTick: Int = 0
)

class EntryViewModel(
    private val repo: LedgerRepository,
    initialKind: String = "EXPENSE",
    private val editingId: Long? = null
) : ViewModel() {

    private val _state = MutableStateFlow(EntryUiState(kind = initialKind, editingId = editingId))
    val state: StateFlow<EntryUiState> = _state.asStateFlow()

    /** 编辑时保留原账单的这几个字段，保存时原样带回去。 */
    private var editingOriginal: TransactionEntity? = null

    /** 全量分类缓存：切换收支时要重新按 kind 过滤，不能只依赖 Flow 的重新发射。 */
    private var allCategories: List<CategoryEntity> = emptyList()

    init {
        if (editingId != null) {
            viewModelScope.launch {
                repo.transactionById(editingId)?.let { tx ->
                    editingOriginal = tx
                    _state.update { s ->
                        s.copy(
                            kind = tx.kind,
                            amountInput = MoneyUtils.formatPlain(tx.amount),
                            note = tx.note,
                            timestamp = tx.timestamp,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            editingId = tx.id
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            repo.categories().collect { cats ->
                allCategories = cats
                _state.update { s ->
                    val filtered = cats.filter { it.kind == s.kind }
                    s.copy(
                        categories = filtered,
                        categoryId = s.categoryId?.takeIf { id -> filtered.any { it.id == id } }
                            ?: filtered.firstOrNull()?.id
                    )
                }
            }
        }
        viewModelScope.launch {
            repo.accounts().collect { accs ->
                _state.update { s ->
                    s.copy(
                        accounts = accs,
                        accountId = s.accountId ?: accs.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun switchKind(kind: String) {
        _state.update { s ->
            val filtered = allCategories.filter { it.kind == kind }
            s.copy(
                kind = kind,
                categories = filtered,
                categoryId = filtered.firstOrNull()?.id
            )
        }
    }

    fun selectCategory(id: Long) {
        _state.update { it.copy(categoryId = id) }
    }

    fun selectAccount(id: Long) {
        _state.update { it.copy(accountId = id) }
    }

    fun setNote(text: String) {
        _state.update { it.copy(note = text) }
    }

    fun setTimestamp(ts: Long) {
        _state.update { it.copy(timestamp = ts) }
    }

    fun pressKey(key: String) {
        _state.update { s ->
            val current = s.amountInput
            val next = when {
                key == "DEL" -> if (current.isEmpty()) "" else current.dropLast(1)
                key == "." -> {
                    when {
                        current.contains('.') -> current
                        current.isEmpty() -> "0."
                        else -> "$current."
                    }
                }
                key in listOf("+", "-") -> current // 算式留到后续版本
                else -> {
                    val dotIndex = current.indexOf('.')
                    val decimals = if (dotIndex >= 0) current.length - dotIndex - 1 else 0
                    when {
                        decimals >= 2 -> current
                        current == "0" -> key
                        current.length >= 9 -> current
                        else -> current + key
                    }
                }
            }
            s.copy(amountInput = next)
        }
    }

    fun save(onDone: (Long) -> Unit) {
        val s = _state.value
        val amount = parseAmount(s.amountInput) ?: return
        val accountId = s.accountId ?: return
        viewModelScope.launch {
            val original = editingOriginal
            if (original != null) {
                // 改账单：金额、分类、账户、时间、备注都能改，其余字段（来源、退款关联、导入批次）保持原样
                val updated = original.copy(
                    kind = s.kind,
                    amount = amount,
                    categoryId = s.categoryId,
                    accountId = accountId,
                    note = s.note,
                    timestamp = s.timestamp
                )
                repo.updateTransaction(original, updated)
                onDone(updated.id)
            } else {
                val entity = TransactionEntity(
                    kind = s.kind,
                    amount = amount,
                    categoryId = s.categoryId,
                    accountId = accountId,
                    note = s.note,
                    timestamp = s.timestamp,
                    source = "MANUAL"
                )
                val id = repo.insertTransaction(entity)
                onDone(id)
            }
        }
    }

    private fun parseAmount(input: String): Long? {
        if (input.isBlank() || input == ".") return null
        val value = input.toDoubleOrNull() ?: return null
        if (value <= 0.0) return null
        return Math.round(value * 100)
    }
}
