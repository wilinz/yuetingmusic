package com.wilinz.yuetingmusic.data.services.user;

import retrofit2.http.GET;

public interface VisitorLoginService {

    @GET("/login")
    void login();
}
