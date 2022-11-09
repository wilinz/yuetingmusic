package com.wilinz.yuetingmusic;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.wilinz.yuetingmusic.util.MediaUtil;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class App extends Application {

    private static String TAG="App";
    public static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


}