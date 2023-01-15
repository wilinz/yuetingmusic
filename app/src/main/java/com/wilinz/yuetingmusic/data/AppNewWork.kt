package com.wilinz.yuetingmusic.data

import com.google.gson.JsonParser
import com.thomasbouvier.persistentcookiejar.PersistentCookieJar
import com.thomasbouvier.persistentcookiejar.cache.SetCookieCache
import com.thomasbouvier.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.wilinz.yuetingmusic.App
import com.wilinz.yuetingmusic.data.services.LoginService
import com.wilinz.yuetingmusic.data.services.MusicInfoService
import com.wilinz.yuetingmusic.data.services.TopListService
import com.wilinz.yuetingmusic.util.IntentUtil
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object AppNewWork {
    var cookieJar =
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(App.instance))

    private val retrofit = Retrofit.Builder()
        .client(OkHttpClient.Builder().apply {
            cookieJar(cookieJar)
            this.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            addInterceptor {
                val response = it.proceed(it.request())

                return@addInterceptor if (response.body != null && response.body?.contentType() != null) {
                    val mediaType = response.body?.contentType()
                    val string = response.body?.string()
                    val data = JsonParser.parseString(string)

                    if (data.isJsonObject && data.asJsonObject["code"].asInt == -462) {
                        IntentUtil.browse(
                            App.instance,
                            data.asJsonObject["data"].asJsonObject["url"].asString
                        )
                    }
                    val responseBody = ResponseBody.create(mediaType, string ?: "")
                    response.newBuilder().body(responseBody).build()
                } else {
                    response
                }
            }
        }.build())
        .baseUrl("https://home.wilinz.com:9999/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
    var topListService = retrofit.create<TopListService>()
    var musicInfoService = retrofit.create<MusicInfoService>()
    var loginService = retrofit.create<LoginService>()
    private inline fun <reified T> Retrofit.create(): T {
        return create(T::class.java)
    }

    val instance get() = this
}