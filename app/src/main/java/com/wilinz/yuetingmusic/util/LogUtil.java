package com.wilinz.yuetingmusic.util;

import android.util.Log;

/**
 * Created by root on 15-11-2.
 */
public class LogUtil {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static final int LEVEL = VERBOSE;

    public static void v(String tag, Object msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, msg.toString());
        }
    }

    public static void d(String tag, Object msg) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, msg.toString());
        }
    }

    public static void i(String tag, Object msg) {
        if (LEVEL <= INFO) {
            Log.i(tag, msg.toString());
        }
    }

    public static void w(String tag, Object msg) {
        if (LEVEL <= WARN) {
            Log.w(tag, msg.toString());
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL <= ERROR) {
            Log.e(tag, msg.toString());
        }
    }
}