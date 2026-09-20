package com.expenser.app.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardViewModelTest {

    @Test fun solvent_highlights_net_worth_as_the_surplus() {
        val breakdown = netWorthBreakdown(totalIncomeMinor = 10_000L, totalExpenseMinor = 4_000L)

        assertEquals(NetWorthBreakdown("Expense", 4_000L, "Net worth", 6_000L), breakdown)
    }

    @Test fun insolvent_highlights_the_overspend_as_the_deficit() {
        val breakdown = netWorthBreakdown(totalIncomeMinor = 4_000L, totalExpenseMinor = 10_000L)

        assertEquals(NetWorthBreakdown("Overspent", 6_000L, "Income", 4_000L), breakdown)
    }

    @Test fun breaking_even_counts_as_solvent() {
        val breakdown = netWorthBreakdown(totalIncomeMinor = 5_000L, totalExpenseMinor = 5_000L)

        assertEquals(NetWorthBreakdown("Expense", 5_000L, "Net worth", 0L), breakdown)
    }
}
