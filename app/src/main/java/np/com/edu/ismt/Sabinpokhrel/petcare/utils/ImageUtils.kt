package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUtils {

    /**
     * Creates a content:// Uri (via FileProvider) pointing to a new file where
     * the camera app should save the captured photo.
     */
    fun createImageUri(context: Context): Uri {
        val imagesDir = File(context.getExternalFilesDir("Pictures"), "")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val file = File(imagesDir, "pet_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun takePersistablePermission(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // Some Uris (e.g. from FileProvider/camera) don't support persistable permissions; ignore.
        }
    }
}
