package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    private val sample = BackupData(
        categories = listOf(
            CategoryEntity("c1", "Food", EntryType.Expense, "Groceries & dining", "u1"),
            CategoryEntity("c2", "Salary", EntryType.Income, null, "u1"),
        ),
        transactions = listOf(
            TransactionEntity("t1", "Coffee", 350, "c1", "2026-09-03", null, EntryType.Expense, "u1"),
            TransactionEntity("t2", "Pay", 5_000_00, "c2", "2026-09-01", "September", EntryType.Income, "u1"),
        ),
    )

    @Test
    fun `encode then decode round-trips the data`() {
        val decoded = BackupCodec.decode(BackupCodec.encode(sample))
        assertEquals(sample.categories, decoded.categories)
        assertEquals(sample.transactions, decoded.transactions)
    }

    @Test
    fun `strings with quotes, commas, newlines and unicode survive`() {
        val tricky = BackupData(
            categories = listOf(CategoryEntity("c\"1", "a,b\nc", EntryType.Expense, "emoji 💸 \"q\"", "u")),
            transactions = listOf(
                TransactionEntity("t\\1", "line1\nline2", -1234, "c\"1", "2026-01-01", "tab\tnote", EntryType.Expense, "u"),
            ),
        )
        val decoded = BackupCodec.decode(BackupCodec.encode(tricky))
        assertEquals(tricky.categories, decoded.categories)
        assertEquals(tricky.transactions, decoded.transactions)
    }

    @Test
    fun `restore re-homing keeps large amounts as Long`() {
        val big = BackupData(
            categories = emptyList(),
            transactions = listOf(
                TransactionEntity("t", "big", 9_999_999_999L, "c", "2026-01-01", null, EntryType.Investment, "u"),
            ),
        )
        assertEquals(9_999_999_999L, BackupCodec.decode(BackupCodec.encode(big)).transactions.single().amountMinor)
    }

    @Test
    fun `decode rejects a non-backup json`() {
        val e = assertThrows(IllegalArgumentException::class.java) {
            BackupCodec.decode("""{"hello":"world"}""")
        }
        assertTrue(e.message!!.contains("backup"))
    }

    @Test
    fun `decode rejects malformed json`() {
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode("{ not json") }
    }

    @Test
    fun `decode tolerates a missing categories array`() {
        val json = """{"version":1,"transactions":[]}"""
        val decoded = BackupCodec.decode(json)
        assertTrue(decoded.categories.isEmpty())
        assertTrue(decoded.transactions.isEmpty())
    }
}
