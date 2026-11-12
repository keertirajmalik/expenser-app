package com.expenser.app.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.CategoryBadge
import com.expenser.app.ui.common.RowDivider
import com.expenser.app.ui.common.SectionLabel
import com.expenser.app.ui.common.SegmentedControl
import com.expenser.app.ui.common.filledFieldColors
import com.expenser.app.ui.common.formatMoneyShort
import com.expenser.app.ui.common.groupedRowShape
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingId = null; sheetOpen = true },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add category")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Search categories") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = filledFieldColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "types") {
                SegmentedControl(
                    options = TypeTab.entries,
                    selected = TypeTab.entries.first { it.type == typeFilter },
                    onSelect = { viewModel.setTypeFilter(it.type) },
                    label = { it.label },
                    // On the grey ground the track needs to be a step darker than it.
                    trackColor = MaterialTheme.colorScheme.outline,
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
                    Text(
                        if (query.isNotBlank()) "No categories match your search."
                        else "No categories yet.\nTap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    )
                }
            } else {
                itemsIndexed(categories, key = { _, it -> it.id }) { index, category ->
                    CategoryRow(
                        category = category,
                        amountMinor = spend[category.id] ?: 0L,
                        showType = typeFilter == null,
                        shape = groupedRowShape(isFirst = index == 0, isLast = index == categories.lastIndex),
                        showDivider = index > 0,
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
    val verb = when (type) {
        EntryType.Expense -> "Spent"
        EntryType.Income -> "Earned"
        EntryType.Investment -> "Invested"
        null -> "Total"
    }
    return if (month == null) "$verb, $period" else "$verb $period"
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    amountMinor: Long,
    showType: Boolean,
    shape: Shape,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    // With every type listed, say which one each row is; the type tab says it otherwise.
    val subtitle = listOfNotNull(
        category.type.name.takeIf { showType },
        category.description,
    ).joinToString(" · ")
    Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.surface) {
        Column {
            if (showDivider) RowDivider()
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryBadge(category.name, category.id, size = 36.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    formatMoneyShort(amountMinor),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (amountMinor == 0L) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
