package com.expenser.app.ui.common

import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DayGroupsTest {

    private val today = LocalDate.of(2026, 11, 12)

    private fun item(id: String, date: String, amountMinor: Long) = TransactionListItem(
        transaction = TransactionEntity(id, id, amountMinor, "c", date, null, EntryType.Expense, "u"),
        categoryName = "Cat",
    )

    @Test fun groups_consecutive_dates_with_subtotals_in_order() {
        val groups = listOf(
            item("a", "2026-11-12", 100),
            item("b", "2026-11-12", 50),
            item("c", "2026-11-09", 25),
        ).groupByDay()

        assertEquals(listOf("2026-11-12", "2026-11-09"), groups.map { it.date })
        assertEquals(listOf(150L, 25L), groups.map { it.totalMinor })
        assertEquals(listOf("a", "b"), groups.first().items.map { it.transaction.id })
    }

    @Test fun empty_list_has_no_groups() {
        assertEquals(emptyList<DayGroup>(), emptyList<TransactionListItem>().groupByDay())
    }

    @Test fun header_names_today_and_yesterday_and_adds_the_year_only_when_it_differs() {
        assertEquals("Today · Thu 12 Nov", dayHeaderLabel("2026-11-12", today, Locale.UK))
        assertEquals("Yesterday · Wed 11 Nov", dayHeaderLabel("2026-11-11", today, Locale.UK))
        assertEquals("Mon 9 Nov", dayHeaderLabel("2026-11-09", today, Locale.UK))
        assertEquals("Wed 31 Dec 2025", dayHeaderLabel("2025-12-31", today, Locale.UK))
    }

    @Test fun short_label_drops_the_date_for_today_and_yesterday() {
        assertEquals("Today", shortDayLabel("2026-11-12", today, Locale.UK))
        assertEquals("Yesterday", shortDayLabel("2026-11-11", today, Locale.UK))
        assertEquals("Tue 10 Nov", shortDayLabel("2026-11-10", today, Locale.UK))
    }

    @Test fun malformed_date_falls_back_to_the_raw_text() {
        assertEquals("not-a-date", dayHeaderLabel("not-a-date", today, Locale.UK))
        assertEquals("not-a-date", shortDayLabel("not-a-date", today, Locale.UK))
    }
}
