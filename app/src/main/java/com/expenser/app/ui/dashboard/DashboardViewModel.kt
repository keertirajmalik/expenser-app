package com.expenser.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.model.EntryType
import com.expenser.app.data.repo.TransactionRepository
import com.expenser.app.ui.common.scopeToMonth
import com.expenser.app.ui.common.totalMinor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

/**
 * Totals and entry counts per type for the selected period - everything the hero card
 * and the three summary tiles show.
 */
data class MonthSummary(
    val incomeMinor: Long = 0L,
    val expenseMinor: Long = 0L,
    val investmentMinor: Long = 0L,
    val incomeCount: Int = 0,
    val expenseCount: Int = 0,
    val investmentCount: Int = 0,
) {
    /** What income leaves once spending and investing are taken out. Negative = overspent. */
    val leftMinor: Long get() = incomeMinor - expenseMinor - investmentMinor

    /**
     * Shares for the split bar, each in 0..1 and summing to 1 when anything happened.
     * The base is income, or the outflow when that exceeds income - so an overspent month
     * shows a full bar of spending and investing with no "left" segment, instead of
     * segments that overflow the track.
     */
    val split: Split
        get() {
            val outflow = expenseMinor + investmentMinor
            val base = maxOf(incomeMinor, outflow)
            if (base <= 0L) return Split(0f, 0f, 0f)
            return Split(
                spent = expenseMinor.toFloat() / base,
                invested = investmentMinor.toFloat() / base,
                left = leftMinor.coerceAtLeast(0L).toFloat() / base,
            )
        }

    /** Whole-number share of income left, or null when there is no income to compare with. */
    val leftPercentOfIncome: Int?
        get() = if (incomeMinor <= 0L) null else (leftMinor * 100 / incomeMinor).toInt()

    data class Split(val spent: Float, val invested: Float, val left: Float)
}

/** One category's share of a type's total. */
data class CategoryShare(val categoryId: String, val name: String, val amountMinor: Long, val percent: Int)

/** The biggest categories plus whatever didn't make the cut, summed. */
data class CategoryBreakdown(
    val top: List<CategoryShare> = emptyList(),
    val otherCount: Int = 0,
    val otherMinor: Long = 0L,
)

/** Pure rule behind [MonthSummary] - no Flow/ViewModel, so it's plain-JUnit testable. */
fun monthSummary(items: List<TransactionListItem>): MonthSummary {
    val byType = items.groupBy { it.transaction.type }
    fun rows(type: EntryType) = byType[type].orEmpty()
    return MonthSummary(
        incomeMinor = rows(EntryType.Income).totalMinor(),
        expenseMinor = rows(EntryType.Expense).totalMinor(),
        investmentMinor = rows(EntryType.Investment).totalMinor(),
        incomeCount = rows(EntryType.Income).size,
        expenseCount = rows(EntryType.Expense).size,
        investmentCount = rows(EntryType.Investment).size,
    )
}

/**
 * Group [type]'s rows by category, largest first, keeping the first [limit]. Grouped by
 * id rather than name: two categories can't share a name today, but the id is what
 * identifies one, and it keys the badge colour.
 */
fun categoryBreakdown(items: List<TransactionListItem>, type: EntryType, limit: Int = 4): CategoryBreakdown {
    val rows = items.filter { it.transaction.type == type }
    val total = rows.totalMinor()
    if (total <= 0L) return CategoryBreakdown()
    val shares = rows
        .groupBy { it.transaction.categoryId }
        .map { (id, group) ->
            val amount = group.totalMinor()
            CategoryShare(id, group.first().categoryName, amount, (amount * 100 / total).toInt())
        }
        .sortedWith(compareByDescending<CategoryShare> { it.amountMinor }.thenBy { it.name.lowercase() })
    val rest = shares.drop(limit)
    return CategoryBreakdown(shares.take(limit), rest.size, rest.sumOf { it.amountMinor })
}

class DashboardViewModel(
    transactions: TransactionRepository,
    /** Global month scope, shared with the transaction lists (null = all time). */
    val selectedMonth: StateFlow<YearMonth?>,
    private val onMonthChange: (YearMonth?) -> Unit,
) : ViewModel() {

    // Every transaction in the selected month, newest first; all cards derive from it.
    private val scoped: StateFlow<List<TransactionListItem>> =
        combine(transactions.observeAllWithCategory(), selectedMonth) { list, month ->
            list.scopeToMonth(month)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<MonthSummary> =
        scoped.map(::monthSummary)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary())

    val topExpenseCategories: StateFlow<CategoryBreakdown> =
        scoped.map { categoryBreakdown(it, EntryType.Expense) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryBreakdown())

    /** The newest few entries of any type, for the "Recent" card. */
    val recent: StateFlow<List<TransactionListItem>> =
        scoped.map { it.take(RECENT_COUNT) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectMonth(month: YearMonth?) = onMonthChange(month)

    companion object {
        private const val RECENT_COUNT = 4

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                DashboardViewModel(
                    app.container.transactionRepository,
                    app.container.selectedMonth,
                    app.container::setSelectedMonth,
                )
            }
        }
    }
}
