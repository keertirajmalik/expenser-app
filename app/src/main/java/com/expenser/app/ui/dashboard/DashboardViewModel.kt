package com.expenser.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.model.EntryType
import com.expenser.app.data.repo.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(transactions: TransactionRepository) : ViewModel() {

    val totalExpenseMinor: StateFlow<Long> =
        transactions.observeTotalMinor(EntryType.Expense)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalInvestmentMinor: StateFlow<Long> =
        transactions.observeTotalMinor(EntryType.Investment)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                DashboardViewModel(app.container.transactionRepository)
            }
        }
    }
}
