package com.wilinz.yuetingmusic.ui.recent

import android.app.Application
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.repository.SongRepository
import com.wilinz.yuetingmusic.ui.playlist.PlaylistViewModel1
import io.reactivex.rxjava3.core.Observable

class RecentListViewModel(application: Application) : PlaylistViewModel1(application) {
    init {
        songsList.subscribe()
    }

    override val songsList: Observable<List<Song>>
        get() = SongRepository.instance!!.getRecentList(-1)
            .doOnNext { songs: List<Song> ->
                songsLiveData.postValue(songs)
                refreshingLiveData.postValue(false)
            }
}