package com.wilinz.yuetingmusic.ui.recent;

import android.app.Application;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.ui.playlist.PlaylistViewModel1;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class RecentListViewModel extends PlaylistViewModel1 {

    public RecentListViewModel(@NonNull Application application) {
        super(application);
        getSongsList().subscribe();
    }

    @Override
    public Observable<List<Song>> getSongsList() {
        return SongRepository.getInstance().getRecentList(-1)
                .doOnNext(songs -> {
                    songsLiveData.postValue(songs);
                    refreshingLiveData.postValue(false);
                });
    }

}