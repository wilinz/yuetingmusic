package com.wilinz.yuetingmusic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wilinz.yuetingmusic.constant.PlayMode;

public class Pref {

    private static volatile Pref instance;
    private final SharedPreferences preferences;

    public Pref(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setPlayMode(int playMode) {
        preferences.edit().putInt(Key.playMode,playMode).apply();
    }

    public int getPlayMode(){
        return preferences.getInt(Key.playMode, PlayMode.ORDERLY);
    }

    public static Pref getInstance(Application context) {
        if (instance == null) {
            synchronized (Pref.class) {
                if (instance == null) {
                    instance = new Pref(context);
                }
            }
        }
        return instance;
    }

}
