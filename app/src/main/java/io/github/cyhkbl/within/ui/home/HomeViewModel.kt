package io.github.cyhkbl.within.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.AccountEntity
import io.github.cyhkbl.within.data.db.CategoryEntity
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class DayGroup(
    val dayKey: String,
    val label: String,
    val items: List<TransactionEntity>,
    val expense: Long,
    val income: Long
)

data class HomeUiState(
    val month: YearMonth = YearMonth.now(),
    val expense: Long = 0L,
    val income: Long = 0L,
    val groups: List<DayGroup> = emptyList(),
    val categories: Map<Long, CategoryEntity> = emptyMap(),
    val accounts: Map<Long, AccountEntity> = emptyMap(),
    val totalBalance: Long = 0L,
    val weekValues: List<Float> = List(7) { 0f },
    val weekDays: List<Int> = emptyList(),
    val weekTotal: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repo: LedgerRepository) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val allTx = month.flatMapLatest { ym ->
        val (from, to) = DateUtils.monthRange(ym)
        repo.transactions(from, to)
    }

    val state: StateFlow<HomeUiState> = combine(
        month,
        allTx,
        repo.allCategories(),
        repo.accountsAll(),
        repo.totalBalance()
    ) { ym, txs, cats, accounts, total ->
        val catMap = cats.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }

        // 退款不增加收入，而是冲抵支出
        val grossExpense = txs.filter { it.kind == "EXPENSE" && !it.excludeFromStats }.sumOf { it.amount }
        val refundSum = txs.filter { it.refundedFromId != null }.sumOf { it.amount }
        val expense = (grossExpense - refundSum).coerceAtLeast(0L)
        val income = txs.filter { it.kind == "INCOME" && !it.excludeFromStats }.sumOf { it.amount }

        val groups = txs.groupBy { DateUtils.dayKey(it.timestamp) }
            .toSortedMap(compareByDescending { it })
            .map { (key, items) ->
                val dayGross = items.filter { it.kind == "EXPENSE" && !it.excludeFromStats }.sumOf { it.amount }
                val dayRefund = items.filter { it.refundedFromId != null }.sumOf { it.amount }
                DayGroup(
                    dayKey = key,
                    label = DateUtils.formatDayHeader(items.first().timestamp),
                    items = items.sortedByDescending { it.timestamp },
                    expense = (dayGross - dayRefund).coerceAtLeast(0L),
                    income = items.filter { it.kind == "INCOME" && !it.excludeFromStats }.sumOf { it.amount }
                )
            }

        val today = LocalDate.now()
        // 「最近 7 日」永远指今天往前七天，和正在浏览的月份无关，
        // 所以单独取一次数据，不能复用当月账单。
        val weekStart = DateUtils.dayRange(today.minusDays(6)).first
        val weekEnd = DateUtils.dayRange(today).second
        val weekTx = repo.transactionsOnce(weekStart, weekEnd)
        val weekValues = (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val (from, to) = DateUtils.dayRange(day)
            val gross = weekTx.filter { it.timestamp in from..to && it.kind == "EXPENSE" && !it.excludeFromStats }
                .sumOf { it.amount }
            val refundsInDay = weekTx.filter { it.timestamp in from..to && it.refundedFromId != null }
                .sumOf { it.amount }
            (gross - refundsInDay).coerceAtLeast(0L).toFloat()
        }
        val weekDays = (6 downTo 0).map { offset ->
            today.minusDays(offset.toLong()).dayOfWeek.value
        }

        HomeUiState(
            month = ym,
            expense = expense,
            income = income,
            groups = groups,
            categories = catMap,
            accounts = accMap,
            totalBalance = total,
            weekValues = weekValues,
            weekDays = weekDays,
            weekTotal = weekValues.sumOf { it.toLong() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun setMonth(target: YearMonth) {
        month.value = target
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun currentMonth() {
        month.value = YearMonth.now()
    }

    suspend fun delete(tx: TransactionEntity) = repo.deleteTransaction(tx)
}
