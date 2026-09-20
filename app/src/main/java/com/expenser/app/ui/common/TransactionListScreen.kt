package com.expenser.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.profile.ScreenTopActions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val DISPLAY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val MONTH_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/**
 * The list UI shared by Expense / Income / Investment. Everything type-specific
 * arrives through parameters or [viewModel]; behaviour (search, filters, sort,
 * month scope, swipe-to-delete, add/edit sheet) is identical.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    title: String,
    noun: String,
    type: EntryType,
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TransactionListViewModel,
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val total by viewModel.totalMinor.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val accent = entryTypeColor(type)

    // Saveable, so a rotation doesn't close the sheet out from under a half-typed entry.
    // Keyed by row id and re-resolved from `items`, which avoids a Saver for the entity.
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                actions = { ScreenTopActions(onCategoriesClick, onProfileClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingId = null; sheetOpen = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add $noun")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MonthSelector(month = month, onChange = viewModel::selectMonth)
            TransactionSearchBar(
                query = filter.query,
                onQueryChange = viewModel::setQuery,
                hasActiveFilters = filter.hasActiveFilters || !filter.isDefaultSort,
                onFilterClick = { showFilters = true },
            )
            ActiveFilterChips(
                filter = filter,
                categories = categories,
                onClearCategory = { viewModel.setCategory(null) },
                onClearDates = { viewModel.setDateRange(null, null) },
                onResetSort = { viewModel.setSort(SortField.Date, SortDirection.Descending) },
            )
            TotalBar(total, accent)
            if (items.isEmpty()) {
                EmptyState(emptyMessage(noun, filter, month))
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    items(items, key = { it.transaction.id }) { item ->
                        SwipeToDeleteBox(onDelete = {
                            val deleted = item.transaction
                            viewModel.delete(deleted)
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "${noun.replaceFirstChar { it.uppercase() }} deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short,
                                )
                                if (res == SnackbarResult.ActionPerformed) viewModel.restore(deleted)
                            }
                        }) {
                            TransactionRow(item, accent) {
                                editingId = item.transaction.id
                                sheetOpen = true
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        TransactionFilterSheet(
            filter = filter,
            categories = categories,
            onCategoryChange = viewModel::setCategory,
            onDateRangeChange = viewModel::setDateRange,
            onSortChange = viewModel::setSort,
            onClearAll = viewModel::clearFilters,
            onDismiss = { showFilters = false },
        )
    }

    if (sheetOpen) {
        val editing = editingId?.let { id -> items.firstOrNull { it.transaction.id == id }?.transaction }
        // An id that no longer resolves means the row went away while the sheet was gone
        // (deleted elsewhere, or filtered out); reopening as a blank "add" would silently
        // turn an edit into a new entry, so close instead.
        if (editingId != null && editing == null) {
            sheetOpen = false
        } else {
            TransactionSheet(
                noun = noun,
                editing = editing,
                categories = categories,
                onDismiss = { sheetOpen = false },
                onSave = { id, name, amountMinor, categoryId, date, note ->
                    viewModel.save(id, name, amountMinor, categoryId, date, note)
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

private fun emptyMessage(noun: String, filter: TransactionFilter, month: YearMonth?): String = when {
    filter.query.isNotBlank() || filter.hasActiveFilters -> "No ${noun}s match your search."
    month != null -> "No ${noun}s in ${month.format(MONTH_LABEL)}.\nTap + to add one."
    else -> "No ${noun}s yet.\nTap + to add one."
}

@Composable
private fun TotalBar(totalMinor: Long, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            formatMoney(totalMinor),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

@Composable
private fun TransactionRow(item: TransactionListItem, accent: Color, onClick: () -> Unit) {
    val txn = item.transaction
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${item.categoryName} · ${LocalDate.parse(txn.date).format(DISPLAY)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                formatMoney(txn.amountMinor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
