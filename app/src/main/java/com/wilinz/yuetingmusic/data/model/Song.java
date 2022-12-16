package com.wilinz.yuetingmusic.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

import kotlin.collections.CollectionsKt;

public class Song extends LitePalSupport implements Parcelable {

    public static Song fromExoPlayerMediaItem(MediaItem mediaItem) {
        Song song = new Song();
        MediaMetadata mediaMetadata = mediaItem.mediaMetadata;
        song.title = mediaMetadata.title + "";
        song.album = mediaMetadata.albumTitle + "";
        song.artist = mediaMetadata.artist + "";
        song.coverImgUrl = mediaMetadata.artworkUri + "";
        if (mediaItem.localConfiguration != null) {
            song.url = mediaItem.localConfiguration.uri + "";
        }
        return song;
    }

    public MediaItem mapToExoPlayerMediaItem() {
        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setAlbumTitle(album)
                .setArtist(artist)
                .setArtworkUri(Uri.parse(coverImgUrl))
                .build();
        return new MediaItem.Builder()
                .setMediaId(uniqueId)
                .setUri(url)
                .setMediaMetadata(mediaMetadata)
                .build();
    }

    public MediaBrowserCompat.MediaItem mapToMediaItem() {
        MediaDescriptionCompat desc =
                new MediaDescriptionCompat.Builder()
                        .setIconUri(Uri.parse(this.coverImgUrl))
                        .setMediaId(this.uniqueId)
                        .setTitle(this.title)
                        .setSubtitle(this.artist)
                        .build();

        return new MediaBrowserCompat.MediaItem(desc,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public MediaMetadataCompat mapToMediaMetadata(long... durationArg) {
        long duration = durationArg.length > 0 ? durationArg[0] : this.duration;
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.uniqueId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, this.coverImgUrl)
                .build();
    }

    public Song() {
    }

    public int id;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int songType) {
        this.songType = songType;
        uniqueId = songType == SONG_TYPE_NETEASECLOUD ? neteaseCloudId + "" : url;
    }

    public final static int SONG_TYPE_LOCAL = 2;
    public final static int SONG_TYPE_NETEASECLOUD = 1;

    @Column(nullable = false, unique = true, index = true)
    private String uniqueId;
    public long neteaseCloudId;
    public long songType = SONG_TYPE_NETEASECLOUD;
    public String title;
    public String artist;
    public String album;
    public String coverImgUrl;
    public String url;
    public long duration;
    public long size;

    public List<FavoriteSong> favoriteSongs;
    public List<RecentSong> recentSongs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (neteaseCloudId != song.neteaseCloudId) return false;
        if (songType != song.songType) return false;
        if (duration != song.duration) return false;
        if (size != song.size) return false;
        if (uniqueId != null ? !uniqueId.equals(song.uniqueId) : song.uniqueId != null)
            return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (artist != null ? !artist.equals(song.artist) : song.artist != null) return false;
        if (album != null ? !album.equals(song.album) : song.album != null) return false;
        if (coverImgUrl != null ? !coverImgUrl.equals(song.coverImgUrl) : song.coverImgUrl != null)
            return false;
        if (url != null ? !url.equals(song.url) : song.url != null) return false;
        if (favoriteSongs != null ? !favoriteSongs.equals(song.favoriteSongs) : song.favoriteSongs != null)
            return false;
        return recentSongs != null ? recentSongs.equals(song.recentSongs) : song.recentSongs == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (uniqueId != null ? uniqueId.hashCode() : 0);
        result = 31 * result + (int) (neteaseCloudId ^ (neteaseCloudId >>> 32));
        result = 31 * result + (int) (songType ^ (songType >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (coverImgUrl != null ? coverImgUrl.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (favoriteSongs != null ? favoriteSongs.hashCode() : 0);
        result = 31 * result + (recentSongs != null ? recentSongs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", uniqueId='" + uniqueId + '\'' +
                ", neteaseCloudId=" + neteaseCloudId +
                ", songType=" + songType +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", coverImgUrl='" + coverImgUrl + '\'' +
                ", url='" + url + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", favoriteSongs=" + favoriteSongs +
                ", recentSongs=" + recentSongs +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.uniqueId);
        dest.writeLong(this.neteaseCloudId);
        dest.writeLong(this.songType);
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeString(this.coverImgUrl);
        dest.writeString(this.url);
        dest.writeLong(this.duration);
        dest.writeLong(this.size);
        dest.writeList(this.favoriteSongs);
        dest.writeList(this.recentSongs);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readInt();
        this.uniqueId = source.readString();
        this.neteaseCloudId = source.readLong();
        this.songType = source.readLong();
        this.title = source.readString();
        this.artist = source.readString();
        this.album = source.readString();
        this.coverImgUrl = source.readString();
        this.url = source.readString();
        this.duration = source.readLong();
        this.size = source.readLong();
        this.favoriteSongs = new ArrayList<FavoriteSong>();
        source.readList(this.favoriteSongs, FavoriteSong.class.getClassLoader());
        this.recentSongs = new ArrayList<RecentSong>();
        source.readList(this.recentSongs, RecentSong.class.getClassLoader());
    }

    protected Song(Parcel in) {
        this.id = in.readInt();
        this.uniqueId = in.readString();
        this.neteaseCloudId = in.readLong();
        this.songType = in.readLong();
        this.title = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.coverImgUrl = in.readString();
        this.url = in.readString();
        this.duration = in.readLong();
        this.size = in.readLong();
        this.favoriteSongs = new ArrayList<FavoriteSong>();
        in.readList(this.favoriteSongs, FavoriteSong.class.getClassLoader());
        this.recentSongs = new ArrayList<RecentSong>();
        in.readList(this.recentSongs, RecentSong.class.getClassLoader());
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
