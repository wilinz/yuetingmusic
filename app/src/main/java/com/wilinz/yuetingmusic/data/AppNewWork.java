package com.wilinz.yuetingmusic.data;

import com.wilinz.yuetingmusic.data.services.user.MusicInfoService;
import com.wilinz.yuetingmusic.data.services.user.TopListService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppNewWork {
    private static AppNewWork instance;

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://cloud-music.pl-fe.cn")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build();

    public TopListService topListService = retrofit.create(TopListService.class);
    public MusicInfoService musicInfoService = retrofit.create(MusicInfoService.class);

    private AppNewWork() {
    }

    public static AppNewWork getInstance() {
        if (instance == null) {
            instance = new AppNewWork();
        }
        return instance;
    }
}