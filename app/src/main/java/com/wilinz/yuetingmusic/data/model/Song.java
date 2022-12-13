package com.wilinz.yuetingmusic.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import org.litepal.crud.LitePalSupport;

public class Song extends LitePalSupport implements Parcelable {

    public MediaItem mapToExoPlayerMediaItem() {
        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setAlbumTitle(album)
                .setArtist(artist)
                .setArtworkUri(Uri.parse(coverImgUrl))
                .build();
        return new MediaItem.Builder()
                .setMediaId(uri.toString())
                .setUri(uri)
                .setMediaMetadata(mediaMetadata)
                .build();
    }

    public MediaBrowserCompat.MediaItem mapToMediaItem() {
        MediaDescriptionCompat desc =
                new MediaDescriptionCompat.Builder()
                        .setIconUri(Uri.parse(this.coverImgUrl))
                        .setMediaId(this.uri.toString())
                        .setTitle(this.title)
                        .setSubtitle(this.artist)
                        .build();

        return new MediaBrowserCompat.MediaItem(desc,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public MediaMetadataCompat mapToMediaMetadata(long... durationArg) {
        long duration = durationArg.length > 0 ? durationArg[0] : this.duration;
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.uri.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, this.coverImgUrl)
                .build();
    }

    public Song() {
    }


    public long neteaseCloudId;
    public String title;
    public String artist;
    public String album;
    public String coverImgUrl;
    public Uri uri;
    public long duration;
    public long size;


    @Override
    public String toString() {
        return "Song{" +
                "neteaseCloudId=" + neteaseCloudId +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", coverImgUrl='" + coverImgUrl + '\'' +
                ", uri=" + uri +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (neteaseCloudId != song.neteaseCloudId) return false;
        if (duration != song.duration) return false;
        if (size != song.size) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (artist != null ? !artist.equals(song.artist) : song.artist != null) return false;
        if (album != null ? !album.equals(song.album) : song.album != null) return false;
        if (coverImgUrl != null ? !coverImgUrl.equals(song.coverImgUrl) : song.coverImgUrl != null)
            return false;
        return uri != null ? uri.equals(song.uri) : song.uri == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (neteaseCloudId ^ (neteaseCloudId >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (coverImgUrl != null ? coverImgUrl.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.neteaseCloudId);
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeString(this.coverImgUrl);
        dest.writeParcelable(this.uri, flags);
        dest.writeLong(this.duration);
        dest.writeLong(this.size);
    }

    public void readFromParcel(Parcel source) {
        this.neteaseCloudId = source.readLong();
        this.title = source.readString();
        this.artist = source.readString();
        this.album = source.readString();
        this.coverImgUrl = source.readString();
        this.uri = source.readParcelable(Uri.class.getClassLoader());
        this.duration = source.readLong();
        this.size = source.readLong();
    }

    protected Song(Parcel in) {
        this.neteaseCloudId = in.readLong();
        this.title = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.coverImgUrl = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.duration = in.readLong();
        this.size = in.readLong();
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
