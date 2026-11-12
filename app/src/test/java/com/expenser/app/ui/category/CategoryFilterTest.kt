package com.expenser.app.ui.category

import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryFilterTest {

    private val all = listOf(
        CategoryEntity("1", "Groceries", EntryType.Expense, "Supermarket runs", "u"),
        CategoryEntity("2", "Salary", EntryType.Income, null, "u"),
        CategoryEntity("3", "Index fund", EntryType.Investment, "Monthly SIP", "u"),
    )

    private fun ids(list: List<CategoryEntity>) = list.map { it.id }

    @Test fun no_query_and_no_type_keeps_everything() {
        assertEquals(listOf("1", "2", "3"), ids(all.filterCategories("", null)))
    }

    @Test fun type_keeps_only_that_type() {
        assertEquals(listOf("2"), ids(all.filterCategories("", EntryType.Income)))
    }

    @Test fun query_matches_name_or_description_case_insensitively() {
        assertEquals(listOf("1"), ids(all.filterCategories("  SUPERMARKET ", null)))
        assertEquals(listOf("3"), ids(all.filterCategories("index", null)))
    }

    @Test fun query_and_type_both_apply() {
        assertEquals(emptyList<String>(), ids(all.filterCategories("salary", EntryType.Expense)))
    }
}
