package com.wilinz.yuetingmusic.util

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.MediaItem
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.MusicUrl.MusicInfo
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.model.TopListSong.PlaylistBean.TracksBean
import org.apache.commons.io.FilenameUtils
import java.io.InputStream

/**
 * 音乐扫描工具
 *
 * @author llw
 */
object MediaUtil {
    /**
     * 扫描系统里面的音频文件，返回一个list集合
     */
    @JvmStatic
    fun getMusicList(context: Context): List<Song> {
        val list: MutableList<Song> = ArrayList()
        // 媒体库查询语句（写一个工具类MusicUtils）
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
            null, MediaStore.Audio.Media.IS_MUSIC
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val url =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        ?: continue
                val song = Song()
                //歌曲名称
                song.title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                //歌手
                song.artist =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                //专辑名
                song.album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                //歌曲路径
                song.url = url
                //歌曲时长
                song.duration =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        .toLong()
                //歌曲大小
                song.size =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                song.coverImgUrl = UriUtil.idToUri(context, R.drawable.icon).toString()
                song.setUniqueId(Song.SONG_TYPE_LOCAL)
                // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                if (song.title!!.contains("-")) {
                    val str = song.title!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    song.artist = str[0]
                    song.title = str[1]
                }
                list.add(song)
            }
            // 释放资源
            cursor.close()
        }
        return list
    }

    @JvmStatic
    @Throws(Exception::class)
    fun saveAudio(context: Context, name: String?, input: InputStream) {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.TITLE, FilenameUtils.getBaseName(name))
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
        val uri = context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            ?: throw Exception("Failed to insert into media database")
        val output = context.contentResolver.openOutputStream(uri)
            ?: throw Exception("Failed to insert into media database")
        input.copyTo(output, 8192)
    }

    fun getMediaBrowserMediaItem(exoMediaItem: MediaItem): MediaBrowserCompat.MediaItem {
        val mediaMetadata = exoMediaItem.mediaMetadata
        val desc = MediaDescriptionCompat.Builder()
            .setMediaId(exoMediaItem.mediaId)
            .setTitle(mediaMetadata.title)
            .setSubtitle(mediaMetadata.subtitle)
            .build()
        return MediaBrowserCompat.MediaItem(
            desc,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }

    @JvmStatic
    fun getMediaMetadataCompat(mediaItem: MediaItem, duration: Long): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaItem.mediaId)
            .putString(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                mediaItem.mediaMetadata.title.toString() + ""
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                mediaItem.mediaMetadata.artist.toString() + ""
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            .putString(
                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                mediaItem.mediaMetadata.artworkUri.toString()
            )
            .build()
    }

    fun getSong(tracks: TracksBean, musicInfo: MusicInfo, isFilterNullURL: Boolean): Song? {
        if (isFilterNullURL && musicInfo.url == null) {
            return null
        }
        val song = Song()
        song.url = musicInfo.url
        song.neteaseCloudId = musicInfo.id
        song.album = tracks.al!!.name
        song.size = musicInfo.size
        song.artist = tracks.ar!![0].name
        song.duration = musicInfo.time
        song.title = tracks.name
        song.coverImgUrl = tracks.al!!.picUrl
        song.setUniqueId(Song.SONG_TYPE_NETEASECLOUD)
        return song
    }

    @JvmStatic
    fun getSongs(
        tracks: List<TracksBean>,
        musicInfoList: List<MusicInfo>,
        isFilterNullURL: Boolean
    ): List<Song?>? {
        val size = Math.min(tracks.size, musicInfoList.size)
        val songArrayList = ArrayList<Song?>(size)
        for (i in 0 until size) {
            val track = tracks[i]
            val musicInfo =
                musicInfoList.firstOrNull { musicInfo1: MusicInfo -> musicInfo1.id == track.id }
                    ?: continue
            val song = getSong(track, musicInfo, isFilterNullURL)
            if (isFilterNullURL && song == null) {
                continue
            }
            songArrayList.add(song)
        }
        return songArrayList
    }
}