package com.expenser.app.ui.common

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * Backs the Expense / Income / Investment lists: they differ only by [type], so
 * search, category/date filtering, sorting and the month scope all live here.
 * One class parameterized by [EntryType], not a subclass per screen - the three
 * screens only ever differed by which [EntryType] they scope their queries to.
 */
class TransactionListViewModel(
    private val type: EntryType,
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
    /** Global month scope, shared with the dashboard (null = all time). */
    val selectedMonth: StateFlow<YearMonth?>,
    private val onMonthChange: (YearMonth?) -> Unit,
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter

    val items: StateFlow<List<TransactionListItem>> =
        combine(transactions.observeByType(type), _filter, selectedMonth) { list, filter, month ->
            list.applyFilter(filter, month)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Total of the currently shown (filtered) rows, so the header matches the list. */
    val totalMinor: StateFlow<Long> =
        items.map { it.totalMinor() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** Categories the picker/filter may offer for this type. */
    val categories: StateFlow<List<CategoryEntity>> =
        categories.observeByType(type)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setCategory(categoryId: String?) = _filter.update { it.copy(categoryId = categoryId) }
    fun setDateRange(start: LocalDate?, end: LocalDate?) =
        _filter.update { it.copy(start = start, end = end) }

    fun setSort(field: SortField, direction: SortDirection) =
        _filter.update { it.copy(sortField = field, sortDirection = direction) }

    /** Clear filters and sort but keep the current search text. */
    fun clearFilters() = _filter.update { TransactionFilter(query = it.query) }

    fun selectMonth(month: YearMonth?) = onMonthChange(month)

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
                transactions.save(id, name, amountMinor, categoryId, date, note, type)
            }.onFailure { _message.value = it.message ?: "Couldn't save ${type.name.lowercase()}." }
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
        fun factory(type: EntryType): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                TransactionListViewModel(
                    type,
                    app.container.transactionRepository,
                    app.container.categoryRepository,
                    app.container.selectedMonth,
                    app.container::setSelectedMonth,
                )
            }
        }
    }
}
