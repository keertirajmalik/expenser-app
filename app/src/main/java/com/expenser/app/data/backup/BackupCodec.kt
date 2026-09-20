package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import java.time.Instant
import java.time.format.DateTimeFormatter

/** The full contents of a backup: every category and transaction. */
data class BackupData(
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
)

/**
 * Reads and writes the `.json` backup file. The format is self-contained and
 * versioned so a restore can validate it before touching the database.
 */
object BackupCodec {

    const val VERSION = 1

    fun encode(data: BackupData): String = buildString {
        append('{')
        append("\"version\":").append(VERSION).append(',')
        append("\"exportedAt\":").append(Json.quote(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))).append(',')
        append("\"categories\":")
        array(data.categories) { c ->
            append('{')
            field("id", c.id); append(',')
            field("name", c.name); append(',')
            field("type", c.type.name); append(',')
            fieldOrNull("description", c.description); append(',')
            field("userId", c.userId)
            append('}')
        }
        append(',')
        append("\"transactions\":")
        array(data.transactions) { t ->
            append('{')
            field("id", t.id); append(',')
            field("name", t.name); append(',')
            append("\"amountMinor\":").append(t.amountMinor); append(',')
            field("categoryId", t.categoryId); append(',')
            field("date", t.date); append(',')
            fieldOrNull("note", t.note); append(',')
            field("type", t.type.name); append(',')
            field("userId", t.userId)
            append('}')
        }
        append('}')
    }

    /** @throws IllegalArgumentException if the text isn't a valid Expenser backup. */
    fun decode(text: String): BackupData {
        val root = Json.parse(text) as? Map<*, *>
            ?: throw IllegalArgumentException("Not an Expenser backup file.")
        require(root.containsKey("transactions")) { "Not an Expenser backup file." }
        val version = (root["version"] as? Number)?.toInt()
            ?: throw IllegalArgumentException("Not an Expenser backup file.")
        require(version <= VERSION) {
            "This backup was made with a newer version of the app and can't be read here."
        }
        val categories = (root["categories"] as? List<*> ?: emptyList<Any?>()).map { it.toCategory() }
        val transactions = (root["transactions"] as? List<*> ?: emptyList<Any?>()).map { it.toTransaction() }
        return BackupData(categories, transactions)
    }

    private inline fun <T> StringBuilder.array(items: List<T>, write: StringBuilder.(T) -> Unit) {
        append('[')
        items.forEachIndexed { index, item ->
            if (index > 0) append(',')
            write(item)
        }
        append(']')
    }

    private fun StringBuilder.field(key: String, value: String) {
        append(Json.quote(key)).append(':').append(Json.quote(value))
    }

    private fun StringBuilder.fieldOrNull(key: String, value: String?) {
        append(Json.quote(key)).append(':').append(value?.let { Json.quote(it) } ?: "null")
    }

    private fun Any?.toCategory(): CategoryEntity {
        val m = asObject()
        return CategoryEntity(
            id = m.str("id"),
            name = m.str("name"),
            type = m.entryType("type"),
            description = m.strOrNull("description"),
            userId = m.strOrNull("userId") ?: "",
        )
    }

    private fun Any?.toTransaction(): TransactionEntity {
        val m = asObject()
        return TransactionEntity(
            id = m.str("id"),
            name = m.str("name"),
            amountMinor = m.long("amountMinor"),
            categoryId = m.str("categoryId"),
            date = m.str("date"),
            note = m.strOrNull("note"),
            type = m.entryType("type"),
            userId = m.strOrNull("userId") ?: "",
        )
    }

    private fun Any?.asObject(): Map<*, *> =
        this as? Map<*, *> ?: throw IllegalArgumentException("Malformed backup entry.")

    private fun Map<*, *>.str(key: String): String =
        this[key] as? String ?: throw IllegalArgumentException("Missing or invalid '$key'.")

    private fun Map<*, *>.strOrNull(key: String): String? = this[key] as? String

    private fun Map<*, *>.entryType(key: String): EntryType {
        val raw = str(key)
        return EntryType.entries.firstOrNull { it.name == raw }
            ?: throw IllegalArgumentException("Unknown '$key' value '$raw'.")
    }

    private fun Map<*, *>.long(key: String): Long =
        (this[key] as? Number)?.toLong() ?: throw IllegalArgumentException("Missing or invalid '$key'.")
}
