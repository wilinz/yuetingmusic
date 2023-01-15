package com.wilinz.yuetingmusic.data.repository

import com.wilinz.yuetingmusic.data.AppNewWork
import com.wilinz.yuetingmusic.data.model.TopList
import com.wilinz.yuetingmusic.data.model.TopListSong
import com.wilinz.yuetingmusic.data.repository.MusicInfoRepository
import io.reactivex.rxjava3.core.Observable

class TopListRepository private constructor() {
    fun get(): Observable<TopList> {
        return AppNewWork.instance.topListService.topList()
    }

    fun getTopListDetails(id: Long): Observable<TopListSong> {
        return AppNewWork.instance.topListService.getTopListDetails(id)
    }

    companion object {
        @Volatile
        private var singleton: TopListRepository? = null
        val instance: TopListRepository?
            get() {
                if (singleton == null) {
                    synchronized(MusicInfoRepository::class.java) {
                        if (singleton == null) {
                            singleton = TopListRepository()
                        }
                    }
                }
                return singleton
            }
    }
}