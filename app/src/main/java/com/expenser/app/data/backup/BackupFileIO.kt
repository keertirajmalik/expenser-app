package com.expenser.app.data.backup

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads/writes the raw text of a backup/export file at a SAF [Uri]. The seam isolates
 * [ContentResolver] I/O behind an interface a test can fake, instead of needing
 * Robolectric or a device. [com.expenser.app.ui.profile.BackupViewModel] still takes
 * a concrete [com.expenser.app.data.repo.BackupRepository], so exercising its full
 * busy/success/error orchestration in a plain JUnit test also needs that repository
 * extracted behind an interface - not done here.
 */
interface BackupFileIO {
    suspend fun write(uri: Uri, text: String)
    suspend fun read(uri: Uri): String
}

/** [BackupFileIO] backed by the real [ContentResolver], for SAF document URIs. */
class ContentResolverBackupFileIO(private val resolver: ContentResolver) : BackupFileIO {
    override suspend fun write(uri: Uri, text: String) = withContext(Dispatchers.IO) {
        resolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            ?: error("No output stream")
    }

    override suspend fun read(uri: Uri): String = withContext(Dispatchers.IO) {
        resolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: error("No input stream")
    }
}
