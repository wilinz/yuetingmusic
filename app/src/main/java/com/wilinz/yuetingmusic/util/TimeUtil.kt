package com.wilinz.yuetingmusic.util

object TimeUtil {
    @JvmStatic
    fun format(curPosition: Long): String {
        val cm = curPosition / 1000 / 60
        val cs = curPosition / 1000 % 60
        val builder = StringBuilder()
        return builder.append(cm / 10).append(cm % 10).append(":")
            .append(cs / 10).append(cs % 10).toString()
    }
}