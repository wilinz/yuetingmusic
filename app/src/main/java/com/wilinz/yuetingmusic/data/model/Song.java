package com.wilinz.yuetingmusic.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Song implements Parcelable {

    public MediaBrowserCompat.MediaItem mapToMediaItem() {
        MediaDescriptionCompat desc =
                new MediaDescriptionCompat.Builder()
                        .setMediaId(this.uri.toString())
                        .setTitle(this.name)
                        .setSubtitle(this.singer)
                        .build();

        return new MediaBrowserCompat.MediaItem(desc,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public MediaMetadataCompat mapToMediaMetadata(long... durationArg) {
        long duration = durationArg.length > 0 ? durationArg[0] : this.duration;
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.uri.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.singer)
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
        if (!Objects.equals(name, song1.name)) return false;
        if (!Objects.equals(singer, song1.singer)) return false;
        if (!Objects.equals(album, song1.album)) return false;
        return Objects.equals(uri, song1.uri);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (singer != null ? singer.hashCode() : 0);
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
                "song='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", path='" + uri + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    public String name;
    public String singer;
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
        dest.writeString(this.name);
        dest.writeString(this.singer);
        dest.writeString(this.album);
        dest.writeParcelable(this.uri, flags);
        dest.writeInt(this.duration);
        dest.writeLong(this.size);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
        this.singer = source.readString();
        this.album = source.readString();
        this.uri = source.readParcelable(Uri.class.getClassLoader());
        this.duration = source.readInt();
        this.size = source.readLong();
    }

    protected Song(Parcel in) {
        this.name = in.readString();
        this.singer = in.readString();
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
