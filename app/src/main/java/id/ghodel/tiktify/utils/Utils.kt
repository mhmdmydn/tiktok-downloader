package id.ghodel.tiktify.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*


class Utils {
    companion object{
        fun getRootDirPath(context: Context): String {
            return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val file: File = ContextCompat.getExternalFilesDirs(
                    context.applicationContext,
                    null
                )[0]
                file.absolutePath
            } else {
                context.applicationContext.filesDir.absolutePath
            }
        }

        fun getProgressDisplayLine(currentBytes: Long, totalBytes: Long): String? {
            return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes)
        }

       private fun getBytesToMBString(bytes: Long): String {
            return java.lang.String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00))
        }

        fun getFileName(url: String, context: Context): String{
            return URLUtil.guessFileName(url, url, context.contentResolver.getType(Uri.parse(url)))
        }
    }
}