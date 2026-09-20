package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.TransactionEntity
import kotlin.math.abs

/** Renders transactions as spreadsheet-friendly CSV (export only, not restored). */
object CsvExport {

    private const val HEADER = "Date,Name,Amount,Category,Type,Note"

    fun transactionsToCsv(data: BackupData): String {
        val categoryName = data.categories.associate { it.id to it.name }
        return buildString {
            append(HEADER).append('\n')
            data.transactions
                .sortedWith(compareByDescending<TransactionEntity> { it.date }.thenBy { it.name.lowercase() })
                .forEach { t ->
                    val row = listOf(
                        t.date,
                        t.name,
                        minorToDecimal(t.amountMinor),
                        categoryName[t.categoryId] ?: "",
                        t.type.name,
                        t.note ?: "",
                    )
                    append(row.joinToString(",") { field(it) }).append('\n')
                }
        }
    }

    /** 1250 -> "12.50", -5 -> "-0.05". Plain decimal, no locale grouping. */
    fun minorToDecimal(minor: Long): String {
        val sign = if (minor < 0) "-" else ""
        val absolute = abs(minor)
        return "$sign${absolute / 100}.${(absolute % 100).toString().padStart(2, '0')}"
    }

    private fun field(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
