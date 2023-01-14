package com.wilinz.yuetingmusic.data.model

import android.net.Uri
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import kotlinx.parcelize.Parcelize
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

@Parcelize
data class Song(
    var id: Int = 0,
    @Column(nullable = false, unique = true, index = true)
    var uniqueId: String? = null,
    var neteaseCloudId: Long = 0,
    var songType: Long = SONG_TYPE_NETEASECLOUD.toLong(),
    var title: String? = null,
    var artist: String? = null,
    var album: String? = null,
    var coverImgUrl: String? = null,
    var url: String? = null,
    var duration: Long = 0,
    var size: Long = 0,
    var favoriteSongs: List<FavoriteSong>? = null,
    var recentSongs: List<RecentSong>? = null,
) : Parcelable,LitePalSupport() {
    fun mapToExoPlayerMediaItem(): MediaItem {
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setAlbumTitle(album)
            .setArtist(artist)
            .apply {
                if (coverImgUrl != null) setArtworkUri(Uri.parse(coverImgUrl))
            }
            .build()
        return MediaItem.Builder()
            .setMediaId(uniqueId!!)
            .setUri(url)
            .setMediaMetadata(mediaMetadata)
            .build()
    }

    fun mapToMediaItem(): MediaBrowserCompat.MediaItem {
        val desc = MediaDescriptionCompat.Builder()
            .setIconUri(Uri.parse(coverImgUrl))
            .setMediaId(uniqueId)
            .setTitle(title)
            .setSubtitle(artist)
            .build()
        return MediaBrowserCompat.MediaItem(
            desc,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }

    fun mapToMediaMetadata(vararg durationArg: Long): MediaMetadataCompat {
        val duration = if (durationArg.isNotEmpty()) durationArg[0] else duration
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, uniqueId)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, coverImgUrl)
            .build()
    }

    fun setUniqueId(songType: Int) {
        this.songType = songType.toLong()
        uniqueId = if (songType == SONG_TYPE_NETEASECLOUD) neteaseCloudId.toString() + "" else url
    }

    companion object {
        fun fromExoPlayerMediaItem(mediaItem: MediaItem): Song {
            val song = Song()
            val mediaMetadata = mediaItem.mediaMetadata
            song.title = mediaMetadata.title.toString() + ""
            song.album = mediaMetadata.albumTitle.toString() + ""
            song.artist = mediaMetadata.artist.toString() + ""
            song.coverImgUrl = mediaMetadata.artworkUri.toString() + ""
            if (mediaItem.localConfiguration != null) {
                song.url = mediaItem.localConfiguration!!.uri.toString() + ""
            }
            return song
        }

        const val SONG_TYPE_LOCAL = 2
        const val SONG_TYPE_NETEASECLOUD = 1
    }
}
