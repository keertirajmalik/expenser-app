package com.expenser.app.ui.common

import com.expenser.app.data.model.EntryType
import java.time.format.DateTimeFormatter

/** "November 2026": the month stepper's label and the lists' "in <month>" phrases. */
val MONTH_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/** What happened to money of this type: "Spent", "Earned", "Invested". */
val EntryType.verb: String
    get() = when (this) {
        EntryType.Expense -> "Spent"
        EntryType.Income -> "Earned"
        EntryType.Investment -> "Invested"
    }

/** "1 entry", "8 entries". */
fun entriesLabel(count: Int): String = if (count == 1) "1 entry" else "$count entries"
