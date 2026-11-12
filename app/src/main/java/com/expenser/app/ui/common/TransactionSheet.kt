package com.expenser.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val DISPLAY: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM yyyy")

private fun parseOrNull(iso: String): LocalDate? = runCatching { LocalDate.parse(iso) }.getOrNull()

/** LocalDate isn't bundleable; round-trip it through its ISO form. */
private val LocalDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = ::parseOrNull,
)

/**
 * Add/edit sheet shared by every transaction type (expense, investment, ...).
 * [noun] is the lowercase entry name used in labels, e.g. "expense" or "investment".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionSheet(
    noun: String,
    editing: TransactionEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, amountMinor: Long, categoryId: String, date: String, note: String?) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // rememberSaveable throughout: a rotation or a process death mid-entry would
    // otherwise discard everything typed into the sheet.
    var name by rememberSaveable { mutableStateOf(editing?.name ?: "") }
    var amount by rememberSaveable { mutableStateOf(editing?.let { minorToInput(it.amountMinor) } ?: "") }
    var categoryId by rememberSaveable { mutableStateOf(editing?.categoryId) }
    var date by rememberSaveable(stateSaver = LocalDateSaver) {
        // Same fallback as the list: a row whose date predates the import validation
        // should open on today rather than crashing the sheet.
        mutableStateOf(editing?.date?.let(::parseOrNull) ?: LocalDate.now())
    }
    var note by rememberSaveable { mutableStateOf(editing?.note ?: "") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val parsedAmount = parseMoneyOrNull(amount)
    val canSave = name.isNotBlank() && parsedAmount != null && parsedAmount > 0 && categoryId != null

    val today = LocalDate.now()
    val fieldColors = filledFieldColors(MaterialTheme.colorScheme.surfaceVariant)
    val fieldShape = RoundedCornerShape(14.dp)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (editing == null) "New $noun" else "Edit $noun",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )

            AmountField(
                value = amount,
                onValueChange = { amount = it },
                isError = amount.isNotBlank() && parsedAmount == null,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Category")
                if (categories.isEmpty()) {
                    Text(
                        "Add a ${noun.replaceFirstChar { it.uppercase() }} category first, from the Categories screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.forEach { category ->
                            PillChip(
                                text = category.name,
                                selected = category.id == categoryId,
                                onClick = { categoryId = category.id },
                                unselectedColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FieldLabel("Date")
                    Text(date.format(DISPLAY), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val choice = when (date) {
                    today -> DateChoice.Today
                    today.minusDays(1) -> DateChoice.Yesterday
                    else -> DateChoice.Other
                }
                SegmentedControl(
                    options = DateChoice.entries,
                    selected = choice,
                    onSelect = {
                        when (it) {
                            DateChoice.Today -> date = today
                            DateChoice.Yesterday -> date = today.minusDays(1)
                            DateChoice.Other -> showDatePicker = true
                        }
                    },
                    label = { it.label },
                    leading = { if (it == DateChoice.Other) Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                shape = fieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    onSave(editing?.id, name, parsedAmount!!, categoryId!!, date.format(ISO), note.ifBlank { null })
                },
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Save $noun", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
            if (editing != null) {
                OutlinedButton(
                    onClick = { onDelete(editing) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("Delete $noun") }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}

private enum class DateChoice(val label: String) {
    Today("Today"),
    Yesterday("Yesterday"),
    Other("Pick date"),
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
}

/**
 * The amount, large and first: it is the one thing every entry needs. The currency
 * symbol sits outside the editable text so the field only ever holds a plain number
 * for [parseMoneyOrNull].
 */
@Composable
private fun AmountField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceVariant)
            // The whole well is the target, not just the digits: the label and the
            // padding around a short number would otherwise swallow the tap.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { focusRequester.requestFocus() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text("Amount", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                currencySymbol(),
                fontSize = 28.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(end = 6.dp),
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text("0", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colors.onSurfaceVariant)
                        }
                        inner()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = "Amount" },
            )
        }
        if (isError) {
            Text("Enter an amount like 640 or 12.50", style = MaterialTheme.typography.bodySmall, color = colors.error)
        }
    }
}
