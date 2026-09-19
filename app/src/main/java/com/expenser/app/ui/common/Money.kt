package com.expenser.app.ui.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)

/** 1250L -> "₹12.50". */
fun formatMoney(amountMinor: Long): String =
    currencyFormat.format(BigDecimal.valueOf(amountMinor).movePointLeft(2))

/** 1250L -> "12.5" — plain editable text for an amount field (no symbol, no trailing zeros). */
fun minorToInput(amountMinor: Long): String =
    BigDecimal.valueOf(amountMinor).movePointLeft(2).stripTrailingZeros().toPlainString()

/**
 * Parse user-typed rupees ("12.5", "12.50", "1,000") into minor units (Long).
 * Returns null for blank/invalid input. Rounds to 2 decimals half-up.
 */
fun parseMoneyOrNull(input: String): Long? {
    val cleaned = input.trim().replace(",", "")
    if (cleaned.isEmpty()) return null
    return try {
        BigDecimal(cleaned)
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
            .takeIf { it >= 0 }
    } catch (e: ArithmeticException) {
        null
    } catch (e: NumberFormatException) {
        null
    }
}
