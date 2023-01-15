package com.wilinz.yuetingmusic.data.services

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LoginService {
    @GET("/login/cellphone?phone=xxx&captcha=1234")
    suspend fun loginByCaptcha(@Query("phone") phone: String, @Query("captcha") captcha: String): Response<*>

    @GET("/captcha/sent")
    suspend fun sendCaptcha(@Query("phone") phone: String): Response<*>
}