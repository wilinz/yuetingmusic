package com.wilinz.yuetingmusic.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object UriUtil {
    @JvmStatic
    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun copyToDir(
        context: Context,
        dir: File,
        uri: Uri,
        filename: String? = getFileName(context, uri)
    ): File {
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, filename)
        val input = context.contentResolver.openInputStream(uri)
        val output: OutputStream = FileOutputStream(file)
        input!!.copyTo(output, 8192)
        input.close()
        output.close()
        return file
    }

    const val RESOURCE = "android.resource://"
    @JvmStatic
    fun idToUri(context: Context, resourceId: Int): Uri {
        return Uri.parse(RESOURCE + context.packageName + "/" + resourceId)
    }
}