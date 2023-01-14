package com.wilinz.yuetingmusic.ui.main.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wilinz.yuetingmusic.data.model.MusicUrl
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.model.TopList
import com.wilinz.yuetingmusic.data.model.TopList.ListBean
import com.wilinz.yuetingmusic.data.model.TopListSong
import com.wilinz.yuetingmusic.data.repository.MusicInfoRepository
import com.wilinz.yuetingmusic.data.repository.SongRepository
import com.wilinz.yuetingmusic.data.repository.TopListRepository
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel
import com.wilinz.yuetingmusic.util.toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class HomeViewModel(application: Application) : MediaControllerViewModel(application) {
    private val songs = MutableLiveData<List<Song>>()
    var refreshingLiveData = MutableLiveData<Boolean>()
    fun getRefreshingLiveData(): LiveData<Boolean> {
        return refreshingLiveData
    }

    fun getSongs(): LiveData<List<Song>> {
        return songs
    }

    private val event = MutableLiveData<Event>()
    fun getEvent(): LiveData<Event> {
        return event
    }

    fun getTopListLiveData(): LiveData<List<ListBean>> {
        return topListLiveData
    }

    private val topListLiveData = MutableLiveData<List<ListBean>>()

    init {
        topList.subscribe()
    }

    @SuppressLint("CheckResult")
    fun getMusics(context: Context) {
        SongRepository.instance!!.getLocalMusic(context)
            .subscribe({ songs1: List<Song>? ->
                val songs2: MutableList<Song> = ArrayList()
                songs2.addAll(songs1!!)
                songs2.addAll(songs1)
                songs.postValue(songs2)
                event.postValue(Event.GetMusicsSuccess)
            }) { e: Throwable ->
                e.printStackTrace()
                toast(getApplication(), "获取数据失败：" + e.message)
            }
    }

    val topList: Observable<TopList>
        get() = TopListRepository.instance!!.get()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { topList: TopList ->
                refreshingLiveData.value = false
                topListLiveData.setValue(topList.list)
            }
            .doOnError { err: Throwable ->
                err.printStackTrace()
                toast(getApplication(), "网络连接失败：" + err.message)
            }

    fun getTopListDetails(index: Int): Observable<TopListSong> {
        return TopListRepository.instance!!.getTopListDetails(topListLiveData.value!![index].id)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { data: TopListSong ->
                val listBean = topListLiveData.value!![index]
                listBean.tracks = data.playlist!!.tracks
            }
    }

    fun getMusicUrls(idList: List<Long>): Observable<MusicUrl> {
        return MusicInfoRepository.instance!!.getMusicUrls(idList)
    }
}