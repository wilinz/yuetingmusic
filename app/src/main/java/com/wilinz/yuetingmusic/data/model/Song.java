package com.wilinz.yuetingmusic.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Song implements Parcelable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song1 = (Song) o;

        if (duration != song1.duration) return false;
        if (size != song1.size) return false;
        if (!Objects.equals(song, song1.song)) return false;
        if (!Objects.equals(singer, song1.singer)) return false;
        if (!Objects.equals(album, song1.album)) return false;
        return Objects.equals(path, song1.path);
    }

    @Override
    public int hashCode() {
        int result = song != null ? song.hashCode() : 0;
        result = 31 * result + (singer != null ? singer.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + duration;
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }

    public Song(){}

    protected Song(Parcel in) {
        song = in.readString();
        singer = in.readString();
        album = in.readString();
        path = in.readString();
        duration = in.readInt();
        size = in.readLong();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "Song{" +
                "song='" + song + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    public String song;
    public String singer;
    public String album;
    public String path;
    public int duration;
    public long size;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(song);
        dest.writeString(singer);
        dest.writeString(album);
        dest.writeString(path);
        dest.writeInt(duration);
        dest.writeLong(size);
    }
}
