package com.wilinz.yuetingmusic.data.services

import com.wilinz.yuetingmusic.data.model.MusicUrl
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicInfoService {
    @GET("/song/url")
    fun getMusicUrls(@Query("id") idList: String?): Observable<MusicUrl>
}