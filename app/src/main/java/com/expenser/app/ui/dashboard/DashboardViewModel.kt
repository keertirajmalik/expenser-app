package com.expenser.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.db.dao.CategorySum
import com.expenser.app.data.db.dao.MonthSum
import com.expenser.app.data.model.EntryType
import com.expenser.app.data.repo.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(private val transactions: TransactionRepository) : ViewModel() {

    val totalExpenseMinor: StateFlow<Long> =
        transactions.observeTotalMinor(EntryType.Expense)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalInvestmentMinor: StateFlow<Long> =
        transactions.observeTotalMinor(EntryType.Investment)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalIncomeMinor: StateFlow<Long> =
        transactions.observeTotalMinor(EntryType.Income)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val incomeByCategory: StateFlow<List<CategorySum>> = categoryChart(EntryType.Income)
    val expenseByCategory: StateFlow<List<CategorySum>> = categoryChart(EntryType.Expense)
    val investmentByCategory: StateFlow<List<CategorySum>> = categoryChart(EntryType.Investment)

    val monthlyExpense: StateFlow<List<MonthSum>> =
        transactions.observeMonthlyTotals(EntryType.Expense)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun categoryChart(type: EntryType): StateFlow<List<CategorySum>> =
        transactions.observeCategoryTotals(type)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                DashboardViewModel(app.container.transactionRepository)
            }
        }
    }
}
