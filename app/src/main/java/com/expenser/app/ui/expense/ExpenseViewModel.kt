package com.expenser.app.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import com.expenser.app.data.repo.CategoryRepository
import com.expenser.app.data.repo.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val TYPE = EntryType.Expense

class ExpenseViewModel(
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
) : ViewModel() {

    val expenses: StateFlow<List<TransactionListItem>> =
        transactions.observeByType(TYPE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalMinor: StateFlow<Long> =
        transactions.observeTotalMinor(TYPE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** Categories the picker may offer for an expense (Expense-type only). */
    val categories: StateFlow<List<CategoryEntity>> =
        categories.observeByType(TYPE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun save(
        id: String?,
        name: String,
        amountMinor: Long,
        categoryId: String,
        date: String,
        note: String?,
    ) {
        viewModelScope.launch {
            runCatching {
                transactions.save(id, name, amountMinor, categoryId, date, note, TYPE)
            }.onFailure { _message.value = it.message ?: "Couldn't save expense." }
        }
    }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch { transactions.delete(transaction) }
    }

    fun restore(transaction: TransactionEntity) {
        viewModelScope.launch { transactions.restore(transaction) }
    }

    fun consumeMessage() { _message.value = null }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                ExpenseViewModel(
                    app.container.transactionRepository,
                    app.container.categoryRepository,
                )
            }
        }
    }
}
