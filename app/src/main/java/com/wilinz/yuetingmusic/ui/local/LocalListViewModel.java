package com.wilinz.yuetingmusic.ui.local;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.ui.playlist.PlaylistViewModel1;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class LocalListViewModel extends PlaylistViewModel1 {

    public LocalListViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public Observable<List<Song>> getSongsList() {
        return SongRepository.getInstance().getLocalMusic(getApplication())
                .doOnNext(songs -> {
                    songsLiveData.postValue(songs);
                    refreshingLiveData.postValue(false);
                });
    }

}