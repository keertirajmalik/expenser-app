package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
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
