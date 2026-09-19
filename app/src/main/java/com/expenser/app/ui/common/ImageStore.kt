package com.expenser.app.ui.common

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Copy a picked image into the app's internal storage and return the file path.
 * Keeps the avatar available offline without holding a content-URI permission.
 */
fun copyImageToInternal(context: Context, uri: Uri): String? = runCatching {
    val dir = File(context.filesDir, "avatars").apply { mkdirs() }
    val dest = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        dest.outputStream().use { output -> input.copyTo(output) }
    } ?: return null
    dest.path
}.getOrNull()
