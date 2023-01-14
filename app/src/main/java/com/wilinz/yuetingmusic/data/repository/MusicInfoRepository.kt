package com.wilinz.yuetingmusic.data.repository

import com.wilinz.yuetingmusic.data.AppNewWork
import com.wilinz.yuetingmusic.data.model.MusicUrl
import io.reactivex.rxjava3.core.Observable
import java.lang.String
import kotlin.Any
import kotlin.Long
import kotlin.synchronized

class MusicInfoRepository private constructor() {
    fun getMusicUrls(idList: List<Long>): Observable<MusicUrl> {
        val query = String.join(",", idList.map { obj: Any -> obj.toString() })
        return AppNewWork.instance!!.musicInfoService.getMusicUrls(query)
    }

    companion object {
        @Volatile
        private var singleton: MusicInfoRepository? = null
        val instance: MusicInfoRepository?
            get() {
                if (singleton == null) {
                    synchronized(MusicInfoRepository::class.java) {
                        if (singleton == null) {
                            singleton = MusicInfoRepository()
                        }
                    }
                }
                return singleton
            }
    }
}