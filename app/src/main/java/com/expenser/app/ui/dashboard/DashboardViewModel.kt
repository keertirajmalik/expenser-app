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

class DashboardViewModel(
    transactions: TransactionRepository,
    /** Global month scope, shared with the transaction lists (null = all time). */
    val selectedMonth: StateFlow<YearMonth?>,
    private val onMonthChange: (YearMonth?) -> Unit,
) : ViewModel() {

    // Every transaction in the selected month; all totals/breakdowns derive from this.
    private val scoped: StateFlow<List<TransactionListItem>> =
        combine(transactions.observeAllWithCategory(), selectedMonth) { list, month ->
            list.scopeToMonth(month)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalExpenseMinor: StateFlow<Long> = totalFor(EntryType.Expense)
    val totalIncomeMinor: StateFlow<Long> = totalFor(EntryType.Income)
    val totalInvestmentMinor: StateFlow<Long> = totalFor(EntryType.Investment)

    val expenseByCategory: StateFlow<List<Pair<String, Long>>> = byCategory(EntryType.Expense)
    val incomeByCategory: StateFlow<List<Pair<String, Long>>> = byCategory(EntryType.Income)
    val investmentByCategory: StateFlow<List<Pair<String, Long>>> = byCategory(EntryType.Investment)

    fun selectMonth(month: YearMonth?) = onMonthChange(month)

    private fun totalFor(type: EntryType): StateFlow<Long> =
        scoped.map { items -> items.filter { it.transaction.type == type }.totalMinor() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private fun byCategory(type: EntryType): StateFlow<List<Pair<String, Long>>> =
        scoped.map { items ->
            items.filter { it.transaction.type == type }
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
