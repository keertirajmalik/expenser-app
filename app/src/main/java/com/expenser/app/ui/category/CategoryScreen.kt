package com.expenser.app.ui.category

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.AddFab
import com.expenser.app.ui.common.EmptyState
import com.expenser.app.ui.common.GroupedListRow
import com.expenser.app.ui.common.SearchField
import com.expenser.app.ui.common.SectionLabel
import com.expenser.app.ui.common.SegmentedControl
import com.expenser.app.ui.common.groupedRowShape
import com.expenser.app.ui.common.verb
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val MONTH_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM")
private val MONTH_WITH_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

/** Options of the type control; [type] null = all. */
private enum class TypeTab(val label: String, val type: EntryType?) {
    All("All", null),
    Expense("Expense", EntryType.Expense),
    Income("Income", EntryType.Income),
    Investment("Investment", EntryType.Investment),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    viewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val spend by viewModel.spendByCategory.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Saveable, so a rotation doesn't close the sheet mid-entry. Keyed by id and
    // re-resolved from `allCategories`, which avoids needing a Saver for the entity.
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { AddFab("Add category") { editingId = null; sheetOpen = true } },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        ) {
            item(key = "search") {
                SearchField(query, viewModel::setQuery, "Search categories", Modifier.fillMaxWidth())
            }
            item(key = "types") {
                SegmentedControl(
                    options = TypeTab.entries,
                    selected = TypeTab.entries.first { it.type == typeFilter },
                    onSelect = { viewModel.setTypeFilter(it.type) },
                    label = { it.label },
                    // On the grey ground the track needs to be a step darker than it.
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            item(key = "header") {
                SectionLabel(
                    countLabel(categories.size, typeFilter),
                    trailing = spendLabel(typeFilter, month),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (categories.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        if (query.isNotBlank()) "No categories match your search."
                        else "No categories yet.\nTap + to add one.",
                    )
                }
            } else {
                itemsIndexed(categories, key = { _, it -> it.id }) { index, category ->
                    val amount = spend[category.id] ?: 0L
                    GroupedListRow(
                        title = category.name,
                        // With every type listed, say which one each row is; the type tab
                        // says it otherwise.
                        subtitle = listOfNotNull(
                            category.type.name.takeIf { typeFilter == null },
                            category.description,
                        ).joinToString(" · "),
                        amountMinor = amount,
                        badgeName = category.name,
                        badgeKey = category.id,
                        shape = groupedRowShape(isFirst = index == 0, isLast = index == categories.lastIndex),
                        showDivider = index > 0,
                        amountColor = if (amount == 0L) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    ) {
                        editingId = category.id
                        sheetOpen = true
                    }
                }
            }
        }
    }

    if (sheetOpen) {
        val editing = editingId?.let { id -> allCategories.firstOrNull { it.id == id } }
        // An id that no longer resolves means the row went away while the sheet was gone;
        // reopening blank would turn an edit into a new entry, so close instead.
        if (editingId != null && editing == null) {
            sheetOpen = false
        } else {
            CategorySheet(
                editing = editing,
                onDismiss = { sheetOpen = false },
                onSave = { id, name, type, description ->
                    viewModel.save(id, name, type, description)
                    sheetOpen = false
                },
                onDelete = {
                    viewModel.delete(it)
                    sheetOpen = false
                },
            )
        }
    }
}

private fun countLabel(count: Int, type: EntryType?): String {
    val noun = type?.name?.lowercase()?.let { "$it categor" } ?: "categor"
    return "$count $noun${if (count == 1) "y" else "ies"}"
}

private fun spendLabel(type: EntryType?, month: YearMonth?): String {
    val period = when {
        month == null -> "all time"
        month.year == YearMonth.now().year -> "in ${month.format(MONTH_SHORT)}"
        else -> "in ${month.format(MONTH_WITH_YEAR)}"
    }
    val verb = type?.verb ?: "Total"
    return if (month == null) "$verb, $period" else "$verb $period"
}
