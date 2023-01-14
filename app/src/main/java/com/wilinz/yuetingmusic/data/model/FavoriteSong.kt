package com.wilinz.yuetingmusic.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

@Parcelize
data class FavoriteSong(
    var id: Int = 0,
    var user: User? = null,
//    @Column(unique = true)
    var song: Song? = null,
    var song_id: Long = 0,
    var timestamp: Long = 0,
    @Column(nullable = false, unique = true, index = true)
    var uniqueId: String? = null
) : LitePalSupport(), Parcelable
