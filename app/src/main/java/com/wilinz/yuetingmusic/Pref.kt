package com.wilinz.yuetingmusic

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.wilinz.yuetingmusic.constant.PlayMode

class Pref(context: Context?) {
    private val preferences: SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    var playMode: Int
        get() = preferences.getInt(Key.playMode, PlayMode.ORDERLY)
        set(playMode) {
            preferences.edit().putInt(Key.playMode, playMode).apply()
        }
    var isFirstLaunch: Boolean
        get() = preferences.getBoolean(Key.isFirstLaunch, true)
        set(firstLaunch) {
            preferences.edit().putBoolean(Key.isFirstLaunch, firstLaunch).apply()
        }

    companion object {
        @Volatile
        private var instance: Pref? = null
        @JvmStatic
        fun getInstance(context: Context): Pref? {
            if (instance == null) {
                synchronized(Pref::class.java) {
                    if (instance == null) {
                        instance = Pref(context.applicationContext)
                    }
                }
            }
            return instance
        }
    }
}