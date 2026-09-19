package com.expenser.app.data.model

/**
 * Discriminator shared by [com.expenser.app.data.db.entity.CategoryEntity] and
 * [com.expenser.app.data.db.entity.TransactionEntity]. A category's type gates which
 * transactions may use it (an Expense transaction only picks Expense categories).
 *
 * Income and Investment exist in the schema but have no UI in v1.
 */
enum class EntryType {
    Expense,
    Income,
    Investment,
}
