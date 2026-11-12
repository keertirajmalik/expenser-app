package com.expenser.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.ui.common.BentoCard
import com.expenser.app.ui.common.RowDivider
import java.time.LocalDate

@Composable
fun BackupSection(
    snackbarHostState: SnackbarHostState,
    viewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory),
) {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var confirmImport by remember { mutableStateOf(false) }

    val exportBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportBackup(it) } }

    val exportCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let { viewModel.exportCsv(it) } }

    val importBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importBackup(it) } }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    BentoCard {
        SettingsRow(
            title = "Export backup",
            subtitle = "Categories and transactions in one file",
            icon = Icons.Filled.Backup,
            enabled = !busy,
            onClick = { exportBackup.launch("expenser-backup-${LocalDate.now()}.json") },
        )
        RowDivider()
        SettingsRow(
            title = "Import backup",
            subtitle = "Replaces everything in the app",
            subtitleColor = MaterialTheme.colorScheme.error,
            icon = Icons.Filled.Restore,
            enabled = !busy,
            onClick = { confirmImport = true },
        )
        RowDivider()
        SettingsRow(
            title = "Export as CSV",
            subtitle = "Transactions as a spreadsheet",
            icon = Icons.Filled.Description,
            enabled = !busy,
            onClick = { exportCsv.launch("expenser-${LocalDate.now()}.csv") },
        )
        if (busy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text("Import backup?") },
            text = { Text("This replaces all current categories and transactions with the contents of the backup file. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmImport = false
                    importBackup.launch(arrayOf("*/*"))
                }) { Text("Choose file") }
            },
            dismissButton = {
                TextButton(onClick = { confirmImport = false }) { Text("Cancel") }
            },
        )
    }
}
