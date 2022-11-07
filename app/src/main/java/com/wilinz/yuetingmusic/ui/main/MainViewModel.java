package com.wilinz.yuetingmusic.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.util.MusicUtils;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<Song>> songs=new MutableLiveData<List<Song>>();

    public LiveData<List<Song>> getSongs(){
        return songs;
    }

    public void getMusics(@NonNull Context context) {
        SongRepository.getMusics(context)
                .subscribe(songs1 -> {
                    songs.postValue(songs1);
                });
    }

}
