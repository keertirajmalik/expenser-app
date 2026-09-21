package com.expenser.app.data.backup

import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import java.time.Instant
import java.time.LocalDate
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

    /**
     * Exactly four digits, two, two. Stricter than [LocalDate.parse], which also accepts
     * signed years outside that width ("+10000-01-01"), because the rest of the app leans
     * on these strings being uniform: range filters compare them lexicographically, and
     * that only equals chronological order while every date is the same shape.
     */
    private val ISO_DATE = Regex("""\d{4}-\d{2}-\d{2}""")

    fun encode(data: BackupData): String = buildString {
        append('{')
        append("\"version\":").append(VERSION).append(',')
        append("\"exportedAt\":").append(Json.quote(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))).append(',')
        append("\"categories\":")
        array(data.categories) { writeFields(Category.all, it) }
        append(',')
        append("\"transactions\":")
        array(data.transactions) { writeFields(Transaction.all, it) }
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

    /**
     * One JSON key with both directions of its translation in a single declaration: where
     * the value comes from, how it is written, and how it is read back.
     *
     * The two directions used to be spelled out separately, which let a key's name or its
     * null handling drift between them - the kind of mistake that writes a backup the app
     * cannot restore, found only when a user tries. Now [encode] walks the field list and
     * [decode] reads through these same objects, so a property is declared once and the
     * compiler rejects a [Field] read into a constructor argument that doesn't exist, or a
     * constructor argument with no [Field] to fill it. The one gap left - declaring a
     * [Field] but leaving it out of [Category.all] / [Transaction.all] - drops the key from
     * the file while [decode] still demands it, which the round-trip test fails on.
     */
    private class Field<E, V>(
        val key: String,
        private val get: (E) -> V,
        private val write: (StringBuilder, V) -> Unit,
        private val read: (Map<*, *>, String) -> V,
    ) {
        fun writeTo(out: StringBuilder, entity: E) {
            out.append(Json.quote(key)).append(':')
            write(out, get(entity))
        }

        fun readFrom(source: Map<*, *>): V = read(source, key)
    }

    private val writeString: (StringBuilder, String) -> Unit = { out, v -> out.append(Json.quote(v)) }

    private val writeStringOrNull: (StringBuilder, String?) -> Unit =
        { out, v -> out.append(v?.let(Json::quote) ?: "null") }

    private val writeLong: (StringBuilder, Long) -> Unit = { out, v -> out.append(v) }

    private val writeEntryType: (StringBuilder, EntryType) -> Unit = { out, v -> out.append(Json.quote(v.name)) }

    /**
     * `all` fixes the order the keys appear in the file. Nothing parses by position, but
     * holding it steady keeps two exports of the same data diffable.
     */
    private object Category {
        val id = Field("id", CategoryEntity::id, writeString) { m, k -> m.str(k) }
        val name = Field("name", CategoryEntity::name, writeString) { m, k -> m.str(k) }
        val type = Field("type", CategoryEntity::type, writeEntryType) { m, k -> m.entryType(k) }
        val description = Field("description", CategoryEntity::description, writeStringOrNull) { m, k -> m.strOrNull(k) }

        // Tolerated when absent, unlike the other strings: BackupRepository overwrites
        // every restored row's userId with whoever is restoring, so the decoded value is
        // discarded and only has to exist.
        val userId = Field("userId", CategoryEntity::userId, writeString) { m, k -> m.strOrNull(k) ?: "" }

        val all = listOf<Field<CategoryEntity, *>>(id, name, type, description, userId)
    }

    private object Transaction {
        val id = Field("id", TransactionEntity::id, writeString) { m, k -> m.str(k) }
        val name = Field("name", TransactionEntity::name, writeString) { m, k -> m.str(k) }
        val amountMinor = Field("amountMinor", TransactionEntity::amountMinor, writeLong) { m, k -> m.long(k) }
        val categoryId = Field("categoryId", TransactionEntity::categoryId, writeString) { m, k -> m.str(k) }
        val date = Field("date", TransactionEntity::date, writeString) { m, k -> m.isoDate(k) }
        val note = Field("note", TransactionEntity::note, writeStringOrNull) { m, k -> m.strOrNull(k) }
        val type = Field("type", TransactionEntity::type, writeEntryType) { m, k -> m.entryType(k) }
        val userId = Field("userId", TransactionEntity::userId, writeString) { m, k -> m.strOrNull(k) ?: "" }

        val all = listOf<Field<TransactionEntity, *>>(id, name, amountMinor, categoryId, date, note, type, userId)
    }

    private inline fun <T> StringBuilder.array(items: List<T>, write: StringBuilder.(T) -> Unit) {
        append('[')
        items.forEachIndexed { index, item ->
            if (index > 0) append(',')
            write(item)
        }
        append(']')
    }

    private fun <E> StringBuilder.writeFields(fields: List<Field<E, *>>, entity: E) {
        append('{')
        fields.forEachIndexed { index, field ->
            if (index > 0) append(',')
            field.writeTo(this, entity)
        }
        append('}')
    }

    private fun Any?.toCategory(): CategoryEntity {
        val m = asObject()
        return CategoryEntity(
            id = Category.id.readFrom(m),
            name = Category.name.readFrom(m),
            type = Category.type.readFrom(m),
            description = Category.description.readFrom(m),
            userId = Category.userId.readFrom(m),
        )
    }

    private fun Any?.toTransaction(): TransactionEntity {
        val m = asObject()
        return TransactionEntity(
            id = Transaction.id.readFrom(m),
            name = Transaction.name.readFrom(m),
            amountMinor = Transaction.amountMinor.readFrom(m),
            categoryId = Transaction.categoryId.readFrom(m),
            date = Transaction.date.readFrom(m),
            note = Transaction.note.readFrom(m),
            type = Transaction.type.readFrom(m),
            userId = Transaction.userId.readFrom(m),
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

    /**
     * A stored date, validated here because this is the only way foreign data enters the
     * database. Everything downstream treats [com.expenser.app.data.db.entity.TransactionEntity.date]
     * as a real ISO date and would otherwise fail far from the cause: the transaction list
     * and the edit sheet both format it, so one bad row used to throw
     * [java.time.format.DateTimeParseException] on every render - and being persisted, it
     * survived restarts, leaving the screen permanently broken.
     */
    private fun Map<*, *>.isoDate(key: String): String {
        val raw = str(key)
        require(ISO_DATE.matches(raw) && runCatching { LocalDate.parse(raw) }.isSuccess) {
            "'$key' must be a yyyy-MM-dd date, but was \"$raw\"."
        }
        return raw
    }
}
