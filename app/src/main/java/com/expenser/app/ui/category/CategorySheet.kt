package com.expenser.app.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.EnumDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySheet(
    editing: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, type: EntryType, description: String?) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // rememberSaveable: don't lose typed input to a rotation mid-entry.
    var name by rememberSaveable { mutableStateOf(editing?.name ?: "") }
    var type by rememberSaveable { mutableStateOf(editing?.type ?: EntryType.Expense) }
    var description by rememberSaveable { mutableStateOf(editing?.description ?: "") }
    val canSave = name.isNotBlank()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (editing == null) "New category" else "Edit category",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            EnumDropdown(
                label = "Type",
                options = EntryType.entries,
                selected = type,
                onSelected = { type = it },
                optionLabel = { it.name },
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (editing != null) {
                    OutlinedButton(
                        onClick = { onDelete(editing) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Delete") }
                }
                Button(
                    onClick = {
                        onSave(editing?.id, name, type, description.ifBlank { null })
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                ) { Text("Save") }
            }
        }
    }
}
