package com.wilinz.yuetingmusic.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.wilinz.yuetingmusic.data.model.Song;

import org.apache.commons.io.FilenameUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import kotlin.io.ByteStreamsKt;

/**
 * 音乐扫描工具
 *
 * @author llw
 */
public class MediaUtil {
    /**
     * 扫描系统里面的音频文件，返回一个list集合
     */
    public static List<Song> getMusicList(Context context) {
        List<Song> list = new ArrayList<Song>();
        // 媒体库查询语句（写一个工具类MusicUtils）
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.Media.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                //歌曲名称
                song.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌手
                song.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //专辑名
                song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                //歌曲路径
                song.uri = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                //歌曲时长
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲大小
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                if (song.size > 1024 * 500) {
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (song.title.contains("-")) {
                        String[] str = song.title.split("-");
                        song.artist = str[0];
                        song.title = str[1];
                    }
                    list.add(song);
                }
            }
            // 释放资源
            cursor.close();
        }
        return list;
    }

    public static void saveAudio(Context context, String name, InputStream input) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.TITLE, FilenameUtils.getBaseName(name));
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null) throw new Exception("Failed to insert into media database");
        OutputStream output = context.getContentResolver().openOutputStream(uri);
        if (output == null) throw new Exception("Failed to insert into media database");
        ByteStreamsKt.copyTo(input, output, 8192);
    }

    public static MediaBrowserCompat.MediaItem getMediaBrowserMediaItem(MediaItem exoMediaItem) {
        MediaMetadata mediaMetadata = exoMediaItem.mediaMetadata;
        MediaDescriptionCompat desc =
                new MediaDescriptionCompat.Builder()
                        .setMediaId(exoMediaItem.mediaId)
                        .setTitle(mediaMetadata.title)
                        .setSubtitle(mediaMetadata.subtitle)
                        .build();

        return new MediaBrowserCompat.MediaItem(desc,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public static MediaMetadataCompat getMediaMetadataCompat(MediaItem mediaItem, long duration){
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaItem.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaItem.mediaMetadata.title+"")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mediaItem.mediaMetadata.artist+"")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,mediaItem.mediaMetadata.artworkUri.toString())
                .build();
    }
}
