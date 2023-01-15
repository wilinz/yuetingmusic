package com.wilinz.yuetingmusic

import android.app.Application
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.litepal.LitePal.initialize

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        setRxJavaErrorHandler()
        initialize(this)
    }

    private fun setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable -> // throwable.printStackTrace();
            throwable.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "App"
        lateinit var instance: Application
    }
}