package com.wilinz.yuetingmusic.util;

public class TimeUtil {
    public static String format(long curPosition) {
        long cm = curPosition / 1000 / 60;
        long cs = curPosition / 1000 % 60;
        StringBuilder builder = new StringBuilder();
        return builder.append(cm / 10).append(cm % 10).append(":")
                .append(cs / 10).append(cs % 10).toString();
    }
}
