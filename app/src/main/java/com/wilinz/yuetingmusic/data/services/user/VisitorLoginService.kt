package com.wilinz.yuetingmusic.data.services.user

import retrofit2.http.GET

interface VisitorLoginService {
    @GET("/login")
    fun login()
}