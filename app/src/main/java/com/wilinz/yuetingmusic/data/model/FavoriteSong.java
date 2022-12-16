package com.wilinz.yuetingmusic.data.model;

import android.net.Uri;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class FavoriteSong extends LitePalSupport {
    public int id;
    public User user;
//    @Column(unique = true)
    public Song song;
    public long song_id;
    public long timestamp;
    @Column(nullable = false, unique = true, index = true)
    public String uniqueId;

    @Override
    public String toString() {
        return "FavoriteSong{" +
                "id=" + id +
                ", user=" + user +
                ", song=" + song +
                ", song_id=" + song_id +
                ", timestamp=" + timestamp +
                '}';
    }
}
