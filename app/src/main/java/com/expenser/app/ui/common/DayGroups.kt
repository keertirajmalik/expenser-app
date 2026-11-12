package com.expenser.app.ui.common

import com.expenser.app.data.db.dao.TransactionListItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One day's rows in a transaction list, with that day's subtotal. */
data class DayGroup(
    /** ISO date, as stored. */
    val date: String,
    val totalMinor: Long,
    val items: List<TransactionListItem>,
)

/**
 * Split an already-sorted list into consecutive runs of the same date. Runs, not a
 * groupBy: the order the caller sorted into is kept, and a date-sorted list yields one
 * group per day in that order.
 */
fun List<TransactionListItem>.groupByDay(): List<DayGroup> {
    val groups = mutableListOf<DayGroup>()
    var run = mutableListOf<TransactionListItem>()
    for (item in this) {
        if (run.isNotEmpty() && run.first().transaction.date != item.transaction.date) {
            groups += DayGroup(run.first().transaction.date, run.totalMinor(), run)
            run = mutableListOf()
        }
        run += item
    }
    if (run.isNotEmpty()) groups += DayGroup(run.first().transaction.date, run.totalMinor(), run)
    return groups
}

/**
 * Header for a day group: "Today · Thu 12 Nov", "Yesterday · Wed 11 Nov", "Mon 9 Nov",
 * with the year only when it isn't the current one. Falls back to the raw text for a
 * date that doesn't parse, rather than throwing inside a LazyColumn row.
 */
fun dayHeaderLabel(iso: String, today: LocalDate = LocalDate.now(), locale: Locale = Locale.getDefault()): String {
    val date = parseIsoOrNull(iso) ?: return iso
    val formatted = date.format(dayFormatter(date, today, locale))
    return when (date) {
        today -> "Today · $formatted"
        today.minusDays(1) -> "Yesterday · $formatted"
        else -> formatted
    }
}

/** Short form for a row's subtitle: "Today", "Yesterday", "Tue 10 Nov". */
fun shortDayLabel(iso: String, today: LocalDate = LocalDate.now(), locale: Locale = Locale.getDefault()): String {
    val date = parseIsoOrNull(iso) ?: return iso
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(dayFormatter(date, today, locale))
    }
}

private fun dayFormatter(date: LocalDate, today: LocalDate, locale: Locale): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (date.year == today.year) "EEE d MMM" else "EEE d MMM yyyy", locale)

private fun parseIsoOrNull(iso: String): LocalDate? = runCatching { LocalDate.parse(iso) }.getOrNull()
