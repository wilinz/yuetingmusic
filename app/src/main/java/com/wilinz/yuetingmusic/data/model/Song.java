package com.wilinz.yuetingmusic.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Song implements Parcelable {

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
