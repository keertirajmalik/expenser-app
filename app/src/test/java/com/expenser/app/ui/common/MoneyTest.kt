package com.expenser.app.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun parses_whole_and_decimals() {
        assertEquals(1250L, parseMoneyOrNull("12.50"))
        assertEquals(1250L, parseMoneyOrNull("12.5"))
        assertEquals(1000L, parseMoneyOrNull("10"))
        assertEquals(100000L, parseMoneyOrNull("1,000"))
    }

    @Test fun rounds_half_up() {
        assertEquals(1235L, parseMoneyOrNull("12.345"))
    }

    @Test fun rejects_bad_input() {
        assertNull(parseMoneyOrNull(""))
        assertNull(parseMoneyOrNull("abc"))
        assertNull(parseMoneyOrNull("-5"))
    }

    @Test fun round_trips_through_format() {
        val minor = parseMoneyOrNull("12.50")!!
        // Format contains the numeric portion regardless of currency symbol/locale glyph.
        assert(formatMoney(minor).contains("12.50")) { formatMoney(minor) }
    }
}
