package com.wilinz.yuetingmusic.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.Objects;

public class Song implements Parcelable {

    public MediaItem mapToExoPlayerMediaItem() {
        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setAlbumTitle(album)
                .setArtist(artist)
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
                .build();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song1 = (Song) o;

        if (duration != song1.duration) return false;
        if (size != song1.size) return false;
        if (!Objects.equals(title, song1.title)) return false;
        if (!Objects.equals(artist, song1.artist)) return false;
        if (!Objects.equals(album, song1.album)) return false;
        return Objects.equals(uri, song1.uri);
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + duration;
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }

    public Song() {
    }

    @NonNull
    @Override
    public String toString() {
        return "Song{" +
                "song='" + title + '\'' +
                ", singer='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", path='" + uri + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    public String title;
    public String artist;
    public String album;
    public Uri uri;
    public int duration;
    public long size;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeParcelable(this.uri, flags);
        dest.writeInt(this.duration);
        dest.writeLong(this.size);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readString();
        this.artist = source.readString();
        this.album = source.readString();
        this.uri = source.readParcelable(Uri.class.getClassLoader());
        this.duration = source.readInt();
        this.size = source.readLong();
    }

    protected Song(Parcel in) {
        this.title = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.duration = in.readInt();
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
