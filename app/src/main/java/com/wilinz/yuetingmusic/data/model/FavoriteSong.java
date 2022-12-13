package com.wilinz.yuetingmusic.data.model;

import android.net.Uri;

import org.litepal.crud.LitePalSupport;

public class FavoriteSong extends LitePalSupport {
    public int id;
    public String username;
    public int songId;
}
