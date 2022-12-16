package com.wilinz.yuetingmusic.util

fun toLongArray(list: List<Long>): LongArray {
    return LongArray(list.size){ list[it] }
}