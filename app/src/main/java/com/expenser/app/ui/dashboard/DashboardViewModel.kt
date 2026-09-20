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
 * How the net-worth donut splits into two slices. Solvent (income covers expense)
 * highlights the remaining headroom; insolvent highlights the overspend. The
 * deficit slice is always the "red" one, the surplus slice always the "green" one -
 * callers only need to pick colors, not decide which label goes where.
 */
data class NetWorthBreakdown(
    val deficitLabel: String,
    val deficitMinor: Long,
    val surplusLabel: String,
    val surplusMinor: Long,
)

/** Pure rule behind [NetWorthBreakdown] — no Flow/ViewModel, so it's plain-JUnit testable. */
fun netWorthBreakdown(totalIncomeMinor: Long, totalExpenseMinor: Long): NetWorthBreakdown {
    val netWorth = totalIncomeMinor - totalExpenseMinor
    return if (netWorth >= 0) {
        NetWorthBreakdown("Expense", totalExpenseMinor, "Net worth", netWorth)
    } else {
        NetWorthBreakdown("Overspent", -netWorth, "Income", totalIncomeMinor)
    }
}

class DashboardViewModel(
    transactions: TransactionRepository,
    /** Global month scope, shared with the transaction lists (null = all time). */
    val selectedMonth: StateFlow<YearMonth?>,
    private val onMonthChange: (YearMonth?) -> Unit,
) : ViewModel() {

    // Every transaction in the selected month, grouped by type; all totals/breakdowns
    // derive from this so the split by type only happens once per emission.
    private val byType: StateFlow<Map<EntryType, List<TransactionListItem>>> =
        combine(transactions.observeAllWithCategory(), selectedMonth) { list, month ->
            list.scopeToMonth(month).groupBy { it.transaction.type }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val totalExpenseMinor: StateFlow<Long> = totalFor(EntryType.Expense)
    val totalIncomeMinor: StateFlow<Long> = totalFor(EntryType.Income)
    val totalInvestmentMinor: StateFlow<Long> = totalFor(EntryType.Investment)

    val netWorthBreakdown: StateFlow<NetWorthBreakdown> =
        combine(totalIncomeMinor, totalExpenseMinor, ::netWorthBreakdown)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), netWorthBreakdown(0L, 0L))

    val expenseByCategory: StateFlow<List<Pair<String, Long>>> = byCategory(EntryType.Expense)
    val incomeByCategory: StateFlow<List<Pair<String, Long>>> = byCategory(EntryType.Income)
    val investmentByCategory: StateFlow<List<Pair<String, Long>>> = byCategory(EntryType.Investment)

    fun selectMonth(month: YearMonth?) = onMonthChange(month)

    private fun totalFor(type: EntryType): StateFlow<Long> =
        byType.map { it[type].orEmpty().totalMinor() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private fun byCategory(type: EntryType): StateFlow<List<Pair<String, Long>>> =
        byType.map { map ->
            map[type].orEmpty()
                .groupBy { it.categoryName }
                .map { (name, rows) -> name to rows.totalMinor() }
                .sortedByDescending { it.second }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
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
