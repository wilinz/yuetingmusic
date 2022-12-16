package com.wilinz.yuetingmusic.data.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class RecentSong extends LitePalSupport {
    public int id;
    public User user;
    public Song song;
    public long song_id;
    public long timestamp;
    @Column(nullable = false, unique = true, index = true)
    public String uniqueId;
}