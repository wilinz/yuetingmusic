package com.wilinz.yuetingmusic.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kotlin.io.ByteStreamsKt;

public class UriUtil {
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static File copyToDir(Context context, File dir, Uri uri) throws IOException {
        return copyToDir(context, dir, uri,UriUtil.getFileName(context, uri));
    }

    public static File copyToDir(Context context, File dir, Uri uri,String filename) throws IOException {
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, filename);
        InputStream input = context.getContentResolver().openInputStream(uri);
        OutputStream output = new FileOutputStream(file);
        ByteStreamsKt.copyTo(input, output, 8192);
        input.close();
        output.close();
        return file;
    }

}
