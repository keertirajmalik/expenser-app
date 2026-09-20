package com.expenser.app.ui.common

import com.expenser.app.data.db.dao.TransactionListItem
import java.time.LocalDate
import java.time.YearMonth

/** Field a transaction list can be sorted by. */
enum class SortField(val label: String) {
    Date("Date"),
    Amount("Amount"),
    Name("Name"),
}

enum class SortDirection { Ascending, Descending }

/**
 * The per-list search / filter / sort state. Time scope comes from the global
 * month (see [applyFilter]); a custom [start]/[end] range overrides that month.
 */
data class TransactionFilter(
    val query: String = "",
    val categoryId: String? = null,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val sortField: SortField = SortField.Date,
    val sortDirection: SortDirection = SortDirection.Descending,
) {
    /** True when anything other than the free-text search narrows the list. */
    val hasActiveFilters: Boolean
        get() = categoryId != null || start != null || end != null

    val isDefaultSort: Boolean
        get() = sortField == SortField.Date && sortDirection == SortDirection.Descending
}

/**
 * Filter + sort a list in memory. Datasets are a single local user's history, so
 * doing this in Kotlin (rather than dynamic SQL) keeps one source of truth and
 * lets the same result drive both the rows and the total.
 *
 * Time window: a custom [TransactionFilter.start]/[end] wins; otherwise the
 * global [month] scopes to that calendar month; a null month means all time.
 */
fun List<TransactionListItem>.applyFilter(
    filter: TransactionFilter,
    month: YearMonth?,
): List<TransactionListItem> {
    val (from, to) = when {
        filter.start != null || filter.end != null -> filter.start to filter.end
        month != null -> month.atDay(1) to month.atEndOfMonth()
        else -> null to null
    }
    val query = filter.query.trim()

    val filtered = filter { item ->
        val txn = item.transaction
        val matchesQuery = query.isEmpty() ||
            txn.name.contains(query, ignoreCase = true) ||
            item.categoryName.contains(query, ignoreCase = true) ||
            (txn.note?.contains(query, ignoreCase = true) == true)
        val matchesCategory = filter.categoryId == null || txn.categoryId == filter.categoryId
        val date = LocalDate.parse(txn.date)
        val matchesRange = (from == null || !date.isBefore(from)) &&
            (to == null || !date.isAfter(to))
        matchesQuery && matchesCategory && matchesRange
    }

    val comparator: Comparator<TransactionListItem> = when (filter.sortField) {
        SortField.Date -> compareBy({ it.transaction.date }, { it.transaction.name.lowercase() })
        SortField.Amount -> compareBy { it.transaction.amountMinor }
        SortField.Name -> compareBy { it.transaction.name.lowercase() }
    }
    return filtered.sortedWith(
        if (filter.sortDirection == SortDirection.Descending) comparator.reversed() else comparator,
    )
}

/** Keep only rows whose date falls in [month]; a null month keeps everything. */
fun List<TransactionListItem>.scopeToMonth(month: YearMonth?): List<TransactionListItem> {
    if (month == null) return this
    val from = month.atDay(1)
    val to = month.atEndOfMonth()
    return filter {
        val date = LocalDate.parse(it.transaction.date)
        !date.isBefore(from) && !date.isAfter(to)
    }
}

/** Sum of the amounts (minor units) in this list. */
fun List<TransactionListItem>.totalMinor(): Long = sumOf { it.transaction.amountMinor }
