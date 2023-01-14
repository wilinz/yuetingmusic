package com.wilinz.yuetingmusic.util

import android.util.Log

/**
 * Created by root on 15-11-2.
 */
object LogUtil {
    const val VERBOSE = 1
    const val DEBUG = 2
    const val INFO = 3
    const val WARN = 4
    const val ERROR = 5
    const val NOTHING = 6
    const val LEVEL = VERBOSE
    fun v(tag: String?, msg: Any) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, msg.toString())
        }
    }

    @JvmStatic
    fun d(tag: String?, msg: Any) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, msg.toString())
        }
    }

    fun i(tag: String?, msg: Any) {
        if (LEVEL <= INFO) {
            Log.i(tag, msg.toString())
        }
    }

    fun w(tag: String?, msg: Any) {
        if (LEVEL <= WARN) {
            Log.w(tag, msg.toString())
        }
    }

    fun e(tag: String?, msg: String) {
        if (LEVEL <= ERROR) {
            Log.e(tag, msg)
        }
    }
}