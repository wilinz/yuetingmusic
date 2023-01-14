package com.wilinz.yuetingmusic.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File

object IntentUtil {
    fun chatWithQQ(context: Context, qq: String): Boolean {
        return try {
            val url = "mqqwpa://im/chat?chat_type=wpa&uin=$qq"
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (exception: Exception) {
            exception.printStackTrace()
            false
        }
    }

    fun joinQQGroup(context: Context, key: String): Boolean {
        val intent = Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key")
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    @JvmOverloads
    fun sendMailTo(
        context: Context,
        sendTo: String,
        title: String? = null,
        content: String? = null
    ): Boolean {
        return try {
            val uri = Uri.parse("mailto:$sendTo")
            val email = arrayOf(sendTo)
            val intent = Intent(Intent.ACTION_SENDTO, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Intent.EXTRA_CC, email)
            if (title != null) intent.putExtra(Intent.EXTRA_SUBJECT, title)
            if (content != null) intent.putExtra(Intent.EXTRA_TEXT, content)
            context.startActivity(Intent.createChooser(intent, ""))
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun browse(context: Context, link: String?): Boolean {
        return try {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (ignored: ActivityNotFoundException) {
            false
        }
    }

    fun shareText(context: Context, text: String?): Boolean {
        return try {
            context.startActivity(
                Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, text)
                    .setType("text/plain")
            )
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    @JvmOverloads
    fun goToAppDetailSettings(
        context: Context,
        packageName: String = context.packageName
    ): Boolean {
        return try {
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.data = Uri.parse("package:$packageName")
            context.startActivity(i)
            true
        } catch (ignored: ActivityNotFoundException) {
            false
        }
    }

    @Throws(ActivityNotFoundException::class)
    fun installApk(context: Context, path: String?, fileProviderAuthority: String?) {
        val uri = getUriOfFile(context, path, fileProviderAuthority)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        context.startActivity(intent)
    }

    fun installApkOrToast(context: Context, path: String?, fileProviderAuthority: String?) {
        try {
            installApk(context, path, fileProviderAuthority)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            //            Toast.makeText(context, R.string.error_activity_not_found_for_apk_installing, Toast.LENGTH_SHORT).show();
        }
    }

    fun viewFile(context: Context, path: String?, fileProviderAuthority: String?): Boolean {
        val mimeType = MimeTypes.fromFileOr(path, "*/*")
        return viewFile(context, path, mimeType, fileProviderAuthority)
    }

    fun getUriOfFile(context: Context?, path: String?, fileProviderAuthority: String?): Uri {
        val uri: Uri
        uri = if (fileProviderAuthority == null) {
            Uri.parse("file://$path")
        } else {
            FileProvider.getUriForFile(context!!, fileProviderAuthority, File(path))
        }
        return uri
    }

    fun viewFile(
        context: Context,
        uri: Uri,
        mimeType: String?,
        fileProviderAuthority: String?
    ): Boolean {
        return if (uri.scheme == "file") {
            viewFile(context, uri.path, mimeType, fileProviderAuthority)
        } else {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, mimeType)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun viewFile(
        context: Context,
        path: String?,
        mimeType: String?,
        fileProviderAuthority: String?
    ): Boolean {
        return try {
            val uri = getUriOfFile(context, path, fileProviderAuthority)
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, mimeType)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            )
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun editFile(context: Context, path: String?, fileProviderAuthority: String?): Boolean {
        return try {
            val mimeType = MimeTypes.fromFileOr(path, "*/*")
            val uri = getUriOfFile(context, path, fileProviderAuthority)
            context.startActivity(
                Intent(Intent.ACTION_EDIT)
                    .setDataAndType(uri, mimeType)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            )
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun requestAppUsagePermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}