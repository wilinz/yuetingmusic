package com.wilinz.yuetingmusic.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wilinz.yuetingmusic.data.model.Song
import io.reactivex.rxjava3.core.Observable

abstract class PlaylistViewModel1(application: Application) : AndroidViewModel(application) {
    fun getSongsLiveData(): LiveData<List<Song>> {
        return songsLiveData
    }

    @JvmField
    var refreshingLiveData = MutableLiveData<Boolean>()
    @JvmField
    var songsLiveData = MutableLiveData<List<Song>>()
    abstract val songsList: Observable<List<Song>>
    fun getRefreshingLiveData(): LiveData<Boolean> {
        return refreshingLiveData
    }
}