package com.wilinz.yuetingmusic.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

@Parcelize
data class User(
    var id: Int = 0,
    @Column(unique = true, index = true)
    var username: String? = null,
    var password: String? = null,
    var nickname: String? = null,
    var avatar: String? = null,
    var rememberPassword: Boolean = false,
    var favoriteSongs: ArrayList<FavoriteSong>? = null,
    var recentSongs: List<RecentSong>? = null,
    @Column
    var isActive: Boolean = false,
) : LitePalSupport(), Parcelable