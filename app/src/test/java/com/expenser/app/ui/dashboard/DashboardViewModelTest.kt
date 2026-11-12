package com.expenser.app.ui.dashboard

import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardViewModelTest {

    private var seq = 0
    private fun item(type: EntryType, amountMinor: Long, categoryId: String = "c", categoryName: String = "Cat") =
        TransactionListItem(
            transaction = TransactionEntity(
                id = "t${seq++}",
                name = "n",
                amountMinor = amountMinor,
                categoryId = categoryId,
                date = "2026-11-01",
                note = null,
                type = type,
                userId = "u",
            ),
            categoryName = categoryName,
        )

    @Test fun summary_totals_and_counts_each_type() {
        val summary = monthSummary(
            listOf(
                item(EntryType.Income, 100_000),
                item(EntryType.Expense, 30_000),
                item(EntryType.Expense, 10_000),
                item(EntryType.Investment, 20_000),
            ),
        )

        assertEquals(MonthSummary(100_000, 40_000, 20_000, 1, 2, 1), summary)
        assertEquals(40_000L, summary.leftMinor)
        assertEquals(40, summary.leftPercentOfIncome)
    }

    @Test fun split_is_shares_of_income_when_solvent() {
        val split = MonthSummary(incomeMinor = 1_000, expenseMinor = 500, investmentMinor = 250).split

        assertEquals(MonthSummary.Split(spent = 0.5f, invested = 0.25f, left = 0.25f), split)
    }

    @Test fun overspent_month_fills_the_bar_with_outflow_and_no_left_segment() {
        val summary = MonthSummary(incomeMinor = 1_000, expenseMinor = 1_500, investmentMinor = 500)

        assertEquals(-1_000L, summary.leftMinor)
        assertEquals(MonthSummary.Split(spent = 0.75f, invested = 0.25f, left = 0f), summary.split)
    }

    @Test fun empty_month_has_an_empty_bar_and_no_percentage() {
        val summary = monthSummary(emptyList())

        assertEquals(MonthSummary.Split(0f, 0f, 0f), summary.split)
        assertNull(summary.leftPercentOfIncome)
    }

    @Test fun breakdown_ranks_categories_and_sums_the_rest() {
        val items = listOf(
            item(EntryType.Expense, 500, "rent", "Rent"),
            item(EntryType.Expense, 200, "food", "Food"),
            item(EntryType.Expense, 100, "food", "Food"),
            item(EntryType.Expense, 100, "bus", "Bus"),
            item(EntryType.Expense, 100, "gym", "Gym"),
            item(EntryType.Income, 9_999, "pay", "Pay"),
        )

        val breakdown = categoryBreakdown(items, EntryType.Expense, limit = 2)

        assertEquals(
            listOf(CategoryShare("rent", "Rent", 500, 50), CategoryShare("food", "Food", 300, 30)),
            breakdown.top,
        )
        assertEquals(2, breakdown.otherCount)
        assertEquals(200L, breakdown.otherMinor)
    }

    @Test fun breakdown_is_empty_without_rows_of_that_type() {
        assertEquals(CategoryBreakdown(), categoryBreakdown(listOf(item(EntryType.Income, 100)), EntryType.Expense))
    }
}
