package com.wilinz.yuetingmusic.data.services.user;

import com.wilinz.yuetingmusic.data.model.TopList;
import com.wilinz.yuetingmusic.data.model.TopListSong;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TopListService {
    @GET("/toplist")
    Observable<TopList> topList();

    @GET("/playlist/detail")
    Observable<TopListSong> getTopListDetails(@Query("id") long id);

}
