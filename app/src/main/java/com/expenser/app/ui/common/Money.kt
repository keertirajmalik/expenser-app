package com.expenser.app.ui.common

import androidx.compose.runtime.mutableStateOf
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Currency codes offered in settings. */
val SUPPORTED_CURRENCIES = listOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "AED")

// Selected currency, as a Compose state so any composable formatting money recomposes on
// change. The single source of truth is AppContainer.currency; this is just the
// Compose-visible cache formatMoney reads, kept in sync via setCurrencyCode.
private val currencyCodeState = mutableStateOf("INR")

fun setCurrencyCode(code: String) { currencyCodeState.value = code }

/** Human label for a currency picker, e.g. "INR (₹)". */
fun currencyLabel(code: String): String =
    runCatching { "$code (${Currency.getInstance(code).symbol})" }.getOrDefault(code)

// Building a NumberFormat is expensive and formatMoney runs per row, per recomposition,
// so keep the last one. Keyed on code + locale: both can change while the app is alive.
// The key is read on every call, so the snapshot read on currencyCodeState still registers.
private var formatKey: Pair<String, Locale>? = null
private var format: NumberFormat? = null

private fun currencyFormat(): NumberFormat {
    val key = currencyCodeState.value to Locale.getDefault()
    format?.let { if (formatKey == key) return it }
    val fresh = NumberFormat.getCurrencyInstance(key.second).apply {
        runCatching { currency = Currency.getInstance(key.first) }
    }
    formatKey = key
    format = fresh
    return fresh
}

/** 1250L -> "₹12.50", using the selected currency. */
fun formatMoney(amountMinor: Long): String =
    currencyFormat().format(BigDecimal.valueOf(amountMinor).movePointLeft(2))

// Whole-unit variant of the cached format, rebuilt only when that one is.
private var wholeFormatBase: NumberFormat? = null
private var wholeFormat: NumberFormat? = null

/**
 * Like [formatMoney], but drops the fraction when it is zero: 4500000L -> "₹45,000",
 * 1250L -> "₹12.50". For summary figures and list rows, where ".00" on every amount is
 * noise and costs width the bento tiles don't have. Never rounds: a non-zero fraction
 * is always shown in full.
 */
fun formatMoneyShort(amountMinor: Long): String {
    if (amountMinor % 100 != 0L) return formatMoney(amountMinor)
    val base = currencyFormat()
    val whole = wholeFormat?.takeIf { wholeFormatBase === base }
        ?: (base.clone() as NumberFormat).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }.also {
            wholeFormatBase = base
            wholeFormat = it
        }
    return whole.format(BigDecimal.valueOf(amountMinor).movePointLeft(2))
}

/** The selected currency's symbol in the current locale, e.g. "₹", for an amount field's prefix. */
fun currencySymbol(): String =
    runCatching { Currency.getInstance(currencyCodeState.value).getSymbol(Locale.getDefault()) }
        .getOrDefault(currencyCodeState.value)

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
