package com.expenser.app.ui.profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.backup.BackupCodec
import com.expenser.app.data.backup.CsvExport
import com.expenser.app.data.repo.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupViewModel(private val backup: BackupRepository) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    fun exportBackup(resolver: ContentResolver, uri: Uri) = run("Couldn't save the backup.") {
        val data = backup.export()
        writeText(resolver, uri, BackupCodec.encode(data))
        "Backup saved · ${data.transactions.size} transactions, ${data.categories.size} categories"
    }

    fun exportCsv(resolver: ContentResolver, uri: Uri) = run("Couldn't export the CSV.") {
        val data = backup.export()
        writeText(resolver, uri, CsvExport.transactionsToCsv(data))
        "CSV exported · ${data.transactions.size} transactions"
    }

    fun importBackup(resolver: ContentResolver, uri: Uri) = run("Couldn't read that backup file.") {
        val data = BackupCodec.decode(readText(resolver, uri))
        backup.restore(data)
        "Restored · ${data.transactions.size} transactions, ${data.categories.size} categories"
    }

    fun consumeMessage() { _message.value = null }

    private fun run(errorMessage: String, block: suspend () -> String) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = runCatching { block() }.getOrElse { errorMessage }
            _busy.value = false
        }
    }

    private suspend fun writeText(resolver: ContentResolver, uri: Uri, text: String) =
        withContext(Dispatchers.IO) {
            resolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                ?: error("No output stream")
        }

    private suspend fun readText(resolver: ContentResolver, uri: Uri): String =
        withContext(Dispatchers.IO) {
            resolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                ?: error("No input stream")
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                BackupViewModel(app.container.backupRepository)
            }
        }
    }
}
