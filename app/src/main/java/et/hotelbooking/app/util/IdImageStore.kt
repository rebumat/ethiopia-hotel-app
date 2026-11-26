package et.hotelbooking.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Identity document images are kept ONLY in app-private internal storage (files/id_docs),
 * never in shared storage or the gallery. They are excluded from cloud backup (see backup rules).
 */
object IdImageStore {
    private fun dir(ctx: Context) = File(ctx.filesDir, "id_docs").apply { mkdirs() }

    /** Creates a private file + content Uri that a camera app can write to (TakePicture contract). */
    fun newCaptureTarget(ctx: Context, side: String): Pair<File, Uri> {
        val f = File(dir(ctx), "id_${side}_${System.currentTimeMillis()}.jpg")
        return f to FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
    }

    /** Copies a picked gallery image into private storage and returns the private path. */
    fun importFromUri(ctx: Context, uri: Uri, side: String): String? = runCatching {
        val f = File(dir(ctx), "id_${side}_${System.currentTimeMillis()}.jpg")
        ctx.contentResolver.openInputStream(uri)!!.use { input -> f.outputStream().use { input.copyTo(it) } }
        f.absolutePath
    }.getOrNull()

    fun delete(path: String) { if (path.isNotBlank()) runCatching { File(path).delete() } }

    /** Masks an ID number for display, keeping only the last 4 characters. */
    fun mask(number: String): String = if (number.length <= 4) "••••" else "•".repeat((number.length - 4).coerceAtMost(8)) + number.takeLast(4)
}
