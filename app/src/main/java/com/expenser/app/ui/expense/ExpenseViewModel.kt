package com.expenser.app.ui.expense

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.model.EntryType
import com.expenser.app.data.repo.CategoryRepository
import com.expenser.app.data.repo.TransactionRepository
import com.expenser.app.ui.common.TransactionListViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.YearMonth

class ExpenseViewModel(
    transactions: TransactionRepository,
    categories: CategoryRepository,
    selectedMonth: StateFlow<YearMonth?>,
    onMonthChange: (YearMonth?) -> Unit,
) : TransactionListViewModel(EntryType.Expense, transactions, categories, selectedMonth, onMonthChange) {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                ExpenseViewModel(
                    app.container.transactionRepository,
                    app.container.categoryRepository,
                    app.container.selectedMonth,
                    app.container::setSelectedMonth,
                )
            }
        }
    }
}
