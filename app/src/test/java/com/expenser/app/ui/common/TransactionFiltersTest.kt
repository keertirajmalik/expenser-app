package com.expenser.app.ui.common

import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class TransactionFiltersTest {

    private fun item(
        name: String,
        amountMinor: Long,
        date: String,
        categoryId: String = "cat-1",
        categoryName: String = "Food",
        note: String? = null,
    ) = TransactionListItem(
        transaction = TransactionEntity(
            id = name,
            name = name,
            amountMinor = amountMinor,
            categoryId = categoryId,
            date = date,
            note = note,
            type = EntryType.Expense,
            userId = "u",
        ),
        categoryName = categoryName,
    )

    private val sample = listOf(
        item("Coffee", 500, "2026-09-03", categoryId = "food", categoryName = "Food"),
        item("Rent", 100_000, "2026-09-01", categoryId = "home", categoryName = "Home", note = "monthly"),
        item("Bus", 200, "2026-08-28", categoryId = "travel", categoryName = "Travel"),
    )

    private fun names(list: List<TransactionListItem>) = list.map { it.transaction.name }

    @Test
    fun `no filter with null month returns all, date desc by default`() {
        val result = sample.applyFilter(TransactionFilter(), month = null)
        assertEquals(listOf("Coffee", "Rent", "Bus"), names(result))
    }

    @Test
    fun `month scope keeps only that calendar month`() {
        val result = sample.applyFilter(TransactionFilter(), month = YearMonth.of(2026, 9))
        assertEquals(listOf("Coffee", "Rent"), names(result))
    }

    @Test
    fun `query matches name, category name and note case-insensitively`() {
        assertEquals(listOf("Coffee"), names(sample.applyFilter(TransactionFilter(query = "coff"), null)))
        assertEquals(listOf("Bus"), names(sample.applyFilter(TransactionFilter(query = "travel"), null)))
        assertEquals(listOf("Rent"), names(sample.applyFilter(TransactionFilter(query = "MONTHLY"), null)))
    }

    @Test
    fun `category filter keeps only that category`() {
        val result = sample.applyFilter(TransactionFilter(categoryId = "home"), null)
        assertEquals(listOf("Rent"), names(result))
    }

    @Test
    fun `custom date range overrides month scope`() {
        val filter = TransactionFilter(
            start = java.time.LocalDate.parse("2026-08-01"),
            end = java.time.LocalDate.parse("2026-08-31"),
        )
        val result = sample.applyFilter(filter, month = YearMonth.of(2026, 9))
        assertEquals(listOf("Bus"), names(result))
    }

    @Test
    fun `sort by amount ascending and descending`() {
        val asc = TransactionFilter(sortField = SortField.Amount, sortDirection = SortDirection.Ascending)
        assertEquals(listOf("Bus", "Coffee", "Rent"), names(sample.applyFilter(asc, null)))
        val desc = asc.copy(sortDirection = SortDirection.Descending)
        assertEquals(listOf("Rent", "Coffee", "Bus"), names(sample.applyFilter(desc, null)))
    }

    @Test
    fun `sort by name ascending is case-insensitive`() {
        val byName = TransactionFilter(sortField = SortField.Name, sortDirection = SortDirection.Ascending)
        assertEquals(listOf("Bus", "Coffee", "Rent"), names(sample.applyFilter(byName, null)))
    }

    @Test
    fun `totalMinor sums the filtered rows`() {
        val septemberTotal = sample.applyFilter(TransactionFilter(), YearMonth.of(2026, 9)).totalMinor()
        assertEquals(100_500, septemberTotal)
    }
}
