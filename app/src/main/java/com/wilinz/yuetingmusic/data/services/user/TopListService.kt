package com.wilinz.yuetingmusic.data.services.user

import com.wilinz.yuetingmusic.data.model.TopList
import com.wilinz.yuetingmusic.data.model.TopListSong
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface TopListService {
    @GET("/toplist")
    fun topList(): Observable<TopList>

    @GET("/playlist/detail")
    fun getTopListDetails(@Query("id") id: Long): Observable<TopListSong>
}