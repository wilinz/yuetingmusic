package com.wilinz.yuetingmusic;

import android.app.Application;
import android.util.Log;

import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.data.repository.UserRepository;

import org.litepal.LitePal;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class App extends Application {

    private static String TAG = "App";
    public static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setRxJavaErrorHandler();
        LitePal.initialize(this);
    }

    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                // throwable.printStackTrace();
                if (null != throwable)
                    throwable.printStackTrace();
            }
        });
    }

}