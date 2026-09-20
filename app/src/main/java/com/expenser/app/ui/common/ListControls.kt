package com.expenser.app.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.expenser.app.data.db.entity.CategoryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val MONTH_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
private val RANGE_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

/**
 * Prev / label / next month stepper. Tapping the label opens a picker that can
 * also choose "All time". Shown on the dashboard and every transaction list.
 */
@Composable
fun MonthSelector(
    month: YearMonth?,
    onChange: (YearMonth?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { month?.let { onChange(it.minusMonths(1)) } }, enabled = month != null) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
        }
        Surface(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = month?.format(MONTH_LABEL) ?: "All time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        IconButton(onClick = { month?.let { onChange(it.plusMonths(1)) } }, enabled = month != null) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
        }
    }

    if (showPicker) {
        MonthPickerDialog(
            initial = month,
            onDismiss = { showPicker = false },
            onPick = {
                onChange(it)
                showPicker = false
            },
        )
    }
}

@Composable
private fun MonthPickerDialog(
    initial: YearMonth?,
    onDismiss: () -> Unit,
    onPick: (YearMonth?) -> Unit,
) {
    var year by remember { mutableStateOf((initial ?: YearMonth.now()).year) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select month", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous year")
                    }
                    Text("$year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next year")
                    }
                }
                (0..3).forEach { rowIndex ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { col ->
                            val monthValue = rowIndex * 3 + col
                            val candidate = YearMonth.of(year, monthValue)
                            FilterChip(
                                selected = candidate == initial,
                                onClick = { onPick(candidate) },
                                label = {
                                    Text(
                                        Month.of(monthValue).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { onPick(null) }) { Text("All time") }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }
}

/** Search field plus a filter button that opens [TransactionFilterSheet]. */
@Composable
fun TransactionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hasActiveFilters: Boolean,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        BadgedBox(badge = { if (hasActiveFilters) Badge() }) {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filters and sort")
            }
        }
    }
}

/** Removable chips summarising the active category / date-range / non-default sort. */
@Composable
fun ActiveFilterChips(
    filter: TransactionFilter,
    categories: List<CategoryEntity>,
    onClearCategory: () -> Unit,
    onClearDates: () -> Unit,
    onResetSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!filter.hasActiveFilters && filter.isDefaultSort) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filter.categoryId?.let { id ->
            val name = categories.firstOrNull { it.id == id }?.name ?: "Category"
            DismissChip(name, onClearCategory)
        }
        if (filter.start != null || filter.end != null) {
            val label = "${filter.start?.format(RANGE_LABEL) ?: "…"} – ${filter.end?.format(RANGE_LABEL) ?: "…"}"
            DismissChip(label, onClearDates)
        }
        if (!filter.isDefaultSort) {
            val arrow = if (filter.sortDirection == SortDirection.Ascending) "↑" else "↓"
            DismissChip("${filter.sortField.label} $arrow", onResetSort)
        }
    }
}

@Composable
private fun DismissChip(label: String, onDismiss: () -> Unit) {
    AssistChip(
        onClick = onDismiss,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Filled.Clear, contentDescription = "Remove", modifier = Modifier.size(16.dp)) },
    )
}

/** Bottom sheet with the category / date-range filters and the sort controls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterSheet(
    filter: TransactionFilter,
    categories: List<CategoryEntity>,
    onCategoryChange: (String?) -> Unit,
    onDateRangeChange: (LocalDate?, LocalDate?) -> Unit,
    onSortChange: (SortField, SortDirection) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRangePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Filters & sort", style = MaterialTheme.typography.titleLarge)

            CategoryFilterDropdown(
                categories = categories,
                selectedId = filter.categoryId,
                onSelected = onCategoryChange,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Date range", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { showRangePicker = true },
                        leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = {
                            Text(
                                if (filter.start == null && filter.end == null) "Any dates"
                                else "${filter.start?.format(RANGE_LABEL) ?: "…"} – ${filter.end?.format(RANGE_LABEL) ?: "…"}",
                            )
                        },
                    )
                    if (filter.start != null || filter.end != null) {
                        TextButton(onClick = { onDateRangeChange(null, null) }) { Text("Clear") }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sort by", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                        SortField.entries.forEachIndexed { index, field ->
                            SegmentedButton(
                                selected = filter.sortField == field,
                                onClick = { onSortChange(field, filter.sortDirection) },
                                shape = SegmentedButtonDefaults.itemShape(index, SortField.entries.size),
                            ) { Text(field.label) }
                        }
                    }
                    val ascending = filter.sortDirection == SortDirection.Ascending
                    IconButton(onClick = {
                        onSortChange(filter.sortField, if (ascending) SortDirection.Descending else SortDirection.Ascending)
                    }) {
                        Icon(
                            if (ascending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = if (ascending) "Ascending" else "Descending",
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onClearAll, modifier = Modifier.weight(1f)) { Text("Clear all") }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Done") }
            }
        }
    }

    if (showRangePicker) {
        DateRangeDialog(
            initialStart = filter.start,
            initialEnd = filter.end,
            onDismiss = { showRangePicker = false },
            onConfirm = { start, end ->
                onDateRangeChange(start, end)
                showRangePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterDropdown(
    categories: List<CategoryEntity>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedId }?.name ?: "All categories"
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All categories") },
                onClick = { onSelected(null); expanded = false },
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelected(category.id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate?, LocalDate?) -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart?.toUtcMillis(),
        initialSelectedEndDateMillis = initialEnd?.toUtcMillis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    state.selectedStartDateMillis?.toUtcLocalDate(),
                    state.selectedEndDateMillis?.toUtcLocalDate(),
                )
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DateRangePicker(state = state, modifier = Modifier.weight(1f))
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
