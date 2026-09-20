package com.expenser.app.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.backup.BackupCodec
import com.expenser.app.data.backup.BackupFileIO
import com.expenser.app.data.backup.ContentResolverBackupFileIO
import com.expenser.app.data.backup.CsvExport
import com.expenser.app.data.repo.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BackupViewModel(
    private val backup: BackupRepository,
    private val fileIO: BackupFileIO,
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    fun exportBackup(uri: Uri) = run("Couldn't save the backup.") {
        val data = backup.export()
        fileIO.write(uri, BackupCodec.encode(data))
        "Backup saved · ${data.transactions.size} transactions, ${data.categories.size} categories"
    }

    fun exportCsv(uri: Uri) = run("Couldn't export the CSV.") {
        val data = backup.export()
        fileIO.write(uri, CsvExport.transactionsToCsv(data))
        "CSV exported · ${data.transactions.size} transactions"
    }

    fun importBackup(uri: Uri) = run("Couldn't read that backup file.") {
        val data = BackupCodec.decode(fileIO.read(uri))
        backup.restore(data)
        "Restored · ${data.transactions.size} transactions, ${data.categories.size} categories"
    }

    fun consumeMessage() { _message.value = null }

    private fun run(errorMessage: String, block: suspend () -> String) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = runCatching { block() }.getOrElse {
                Log.e(TAG, errorMessage, it)
                errorMessage
            }
            _busy.value = false
        }
    }

    companion object {
        private const val TAG = "BackupViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                BackupViewModel(
                    app.container.backupRepository,
                    ContentResolverBackupFileIO(app.contentResolver),
                )
            }
        }
    }
}
