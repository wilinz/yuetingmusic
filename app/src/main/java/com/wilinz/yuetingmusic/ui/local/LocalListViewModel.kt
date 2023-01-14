package com.wilinz.yuetingmusic.ui.local

import android.app.Application
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.repository.SongRepository
import com.wilinz.yuetingmusic.ui.playlist.PlaylistViewModel1
import io.reactivex.rxjava3.core.Observable

class LocalListViewModel(application: Application) : PlaylistViewModel1(application) {
    override val songsList: Observable<List<Song>>
        get() {
            return SongRepository.instance!!.getLocalMusic(getApplication())
                .doOnNext { songs: List<Song> ->
                    songsLiveData.postValue(songs)
                    refreshingLiveData.postValue(false)
                }
        }

}