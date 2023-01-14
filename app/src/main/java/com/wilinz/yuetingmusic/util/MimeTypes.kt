package com.wilinz.yuetingmusic.util

import android.text.TextUtils
import android.webkit.MimeTypeMap
import org.apache.commons.io.FilenameUtils

/**
 * Created by Stardust on 2018/2/12.
 */
object MimeTypes {
    fun fromFile(path: String?): String? {
        val ext = FilenameUtils.getExtension(path)
        return if (TextUtils.isEmpty(ext)) "*/*" else MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(ext)
    }

    fun fromFileOr(path: String?, defaultType: String): String {
        val mimeType = fromFile(path)
        return mimeType ?: defaultType
    }
}