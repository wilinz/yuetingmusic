package com.wilinz.yuetingmusic.data

import com.wilinz.yuetingmusic.data.services.user.MusicInfoService
import com.wilinz.yuetingmusic.data.services.user.TopListService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class AppNewWork private constructor() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://cloud-music.pl-fe.cn")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
    var topListService = retrofit.create(TopListService::class.java)
    var musicInfoService = retrofit.create(MusicInfoService::class.java)

    companion object {
        var instance: AppNewWork? = null
            get() {
                if (field == null) {
                    field = AppNewWork()
                }
                return field
            }
            private set
    }
}