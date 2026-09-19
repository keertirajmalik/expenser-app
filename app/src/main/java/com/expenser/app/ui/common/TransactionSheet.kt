package com.expenser.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val DISPLAY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

/**
 * Add/edit sheet shared by every transaction type (expense, investment, ...).
 * [noun] is the lowercase entry name used in labels, e.g. "expense" or "investment".
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var amount by remember { mutableStateOf(editing?.let { minorToInput(it.amountMinor) } ?: "") }
    var categoryId by remember { mutableStateOf(editing?.categoryId) }
    var date by remember { mutableStateOf(editing?.date?.let { LocalDate.parse(it) } ?: LocalDate.now()) }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedAmount = parseMoneyOrNull(amount)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    val canSave = name.isNotBlank() && parsedAmount != null && parsedAmount > 0 && categoryId != null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (editing == null) "New $noun" else "Edit $noun",
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                singleLine = true,
                isError = amount.isNotBlank() && parsedAmount == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            EnumDropdown(
                label = "Category",
                options = categories,
                selected = selectedCategory,
                onSelected = { categoryId = it.id },
                optionLabel = { it.name },
                placeholder = if (categories.isEmpty()) "Add a ${noun.replaceFirstChar { it.uppercase() }} category first" else "Select",
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date.format(DISPLAY),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (editing != null) {
                    OutlinedButton(onClick = { onDelete(editing) }, modifier = Modifier.weight(1f)) {
                        Text("Delete")
                    }
                }
                Button(
                    onClick = {
                        onSave(editing?.id, name, parsedAmount!!, categoryId!!, date.format(ISO), note.ifBlank { null })
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                ) { Text("Save") }
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
