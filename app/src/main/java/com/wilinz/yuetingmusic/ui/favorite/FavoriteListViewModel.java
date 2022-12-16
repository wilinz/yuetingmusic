package com.wilinz.yuetingmusic.ui.favorite;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wilinz.yuetingmusic.data.model.FavoriteSong;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.data.repository.UserRepository;
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel;
import com.wilinz.yuetingmusic.ui.playlist.PlaylistViewModel1;

import org.litepal.LitePal;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import kotlin.collections.CollectionsKt;

public class FavoriteListViewModel extends PlaylistViewModel1 {

    public FavoriteListViewModel(@NonNull Application application) {
        super(application);
        getSongsList().subscribe();
    }

    @Override
    public Observable<List<Song>> getSongsList() {
        return SongRepository.getInstance().getFavoriteList()
                .doOnNext(songs -> {
                    songsLiveData.postValue(songs);
                    refreshingLiveData.postValue(false);
                });
    }

}