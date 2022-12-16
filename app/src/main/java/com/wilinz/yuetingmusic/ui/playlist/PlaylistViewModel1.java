package com.wilinz.yuetingmusic.ui.playlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public abstract class PlaylistViewModel1 extends AndroidViewModel {

    public PlaylistViewModel1(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Song>> getSongsLiveData() {
        return songsLiveData;
    }

    protected MutableLiveData<Boolean> refreshingLiveData=new MutableLiveData<>();
    protected MutableLiveData<List<Song>> songsLiveData = new MutableLiveData<>();

    public abstract Observable<List<Song>> getSongsList();

    public LiveData<Boolean> getRefreshingLiveData() {
        return refreshingLiveData;
    }
}