package com.wilinz.yuetingmusic;

import android.app.Application;

import org.litepal.LitePal;

public class App extends Application {

    private static String TAG="App";
    public static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LitePal.initialize(this);
    }


}