package com.wilinz.yuetingmusic.data.services.user;

import com.wilinz.yuetingmusic.data.model.MusicUrl;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MusicInfoService {
    @GET("/song/url")
    Observable<MusicUrl> getMusicUrls(@Query("id") String idList);
}
