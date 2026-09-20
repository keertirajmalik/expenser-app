package com.expenser.app.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

/** Round avatar: the stored image if present and readable, else the name's initials. */
@Composable
fun Avatar(
    name: String,
    imagePath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val targetPx = with(LocalDensity.current) { size.roundToPx() }
    val bitmap by decodeAvatar(imagePath, targetPx)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Text(
                initials(name),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value / 2.6f).sp,
            )
        }
    }
}

private fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

/**
 * Load [imagePath] asynchronously, downsampled to roughly [targetPx].
 *
 * A gallery photo is routinely 12MP, which is ~48MB as ARGB_8888 - decoding it whole,
 * on the main thread, to fill a 32-96dp circle risked both jank and OutOfMemoryError.
 * Read the bounds first, then decode with an inSampleSize that lands at or just above
 * the drawn size.
 */
@Composable
private fun decodeAvatar(imagePath: String?, targetPx: Int): State<Bitmap?> =
    produceState<Bitmap?>(initialValue = null, imagePath, targetPx) {
        value = imagePath?.let { path ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(path).takeIf { it.exists() } ?: return@runCatching null
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.path, bounds)
                    BitmapFactory.decodeFile(
                        file.path,
                        BitmapFactory.Options().apply {
                            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, targetPx)
                        },
                    )
                }.getOrNull()
            }
        }
    }

/** Largest power-of-two shrink that keeps the shorter edge >= [targetPx]. */
private fun sampleSize(width: Int, height: Int, targetPx: Int): Int {
    if (width <= 0 || height <= 0 || targetPx <= 0) return 1
    val shorter = minOf(width, height)
    var sample = 1
    while (shorter / (sample * 2) >= targetPx) sample *= 2
    return max(1, sample)
}
