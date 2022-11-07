package com.wilinz.yuetingmusic.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.util.MusicUtils;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SongRepository {

    public static Observable<List<Song>> getMusics(@NonNull Context context) {
        return Observable.just(MusicUtils.getMusicList(context))
                .subscribeOn(Schedulers.io());
    }

}
