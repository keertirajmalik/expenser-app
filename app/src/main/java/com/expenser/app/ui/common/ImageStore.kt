package com.expenser.app.ui.common

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val AVATAR_DIR = "avatars"

/**
 * Copy a picked image into the app's internal storage and return the file path.
 * Keeps the avatar available offline without holding a content-URI permission.
 *
 * Suspending on [Dispatchers.IO]: this reads through a ContentResolver and copies the
 * whole file, which can be tens of megabytes from a camera roll.
 */
suspend fun copyImageToInternal(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    runCatching {
        val dir = File(context.filesDir, AVATAR_DIR).apply { mkdirs() }
        val dest = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
        val copied = context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
            true
        } ?: false
        if (copied) dest.path else null
    }.getOrNull()
}

/**
 * Delete every stored avatar except [keep].
 *
 * Each pick writes a new timestamped file, so without this they accumulate for the life
 * of the install - one per photo the user ever tried, whether or not they saved it.
 * Call after the new path has been persisted, so a failed save can't strand the profile
 * pointing at a file that is already gone.
 */
suspend fun pruneAvatars(context: Context, keep: String?) = withContext(Dispatchers.IO) {
    runCatching {
        File(context.filesDir, AVATAR_DIR)
            .listFiles()
            ?.forEach { if (it.path != keep) it.delete() }
    }
    Unit
}
