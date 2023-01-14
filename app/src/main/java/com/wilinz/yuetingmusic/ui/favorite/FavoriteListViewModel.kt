package com.wilinz.yuetingmusic.ui.favorite

import android.app.Application
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.repository.SongRepository
import com.wilinz.yuetingmusic.ui.playlist.PlaylistViewModel1
import io.reactivex.rxjava3.core.Observable

class FavoriteListViewModel(application: Application) : PlaylistViewModel1(application) {
    init {
        songsList.subscribe()
    }

    override val songsList: Observable<List<Song>>
        get() {
            return SongRepository.instance!!.favoriteList
                .doOnNext { songs: List<Song> ->
                    songsLiveData.postValue(songs)
                    refreshingLiveData.postValue(false)
                }
        }

}