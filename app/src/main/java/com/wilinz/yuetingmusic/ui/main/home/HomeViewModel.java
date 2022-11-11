package com.wilinz.yuetingmusic.ui.main.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.util.MediaUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Song>> songs = new MutableLiveData<>();

    public LiveData<List<Song>> getSongs() {
        return songs;
    }

    private MutableLiveData<Event> event = new MutableLiveData<>();

    public LiveData<Event> getEvent() {
        return event;
    }

    public void getMusics(@NonNull Context context) {
        SongRepository.getMusics(context)
                .subscribe(songs1 -> {
                    List<Song> songs2 = new ArrayList<>();
                    songs2.addAll(songs1);
                    songs2.addAll(songs1);
                    songs.postValue(songs2);
                    event.postValue(Event.GetMusicsSuccess);
                });
    }
}
