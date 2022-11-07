package com.wilinz.yuetingmusic.data.model;

import androidx.annotation.NonNull;

public class Song {

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
}
