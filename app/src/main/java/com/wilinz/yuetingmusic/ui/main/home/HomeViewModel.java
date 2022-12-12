package com.wilinz.yuetingmusic.ui.main.home;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.data.AppNewWork;
import com.wilinz.yuetingmusic.data.model.MusicUrl;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.model.TopList;
import com.wilinz.yuetingmusic.data.model.TopListSong;
import com.wilinz.yuetingmusic.data.repository.MusicInfoRepository;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.data.repository.TopListRepository;
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel;
import com.wilinz.yuetingmusic.util.ToastUtilKt;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class HomeViewModel extends MediaControllerViewModel {

    private MutableLiveData<List<Song>> songs = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        getTopList();
    }

    public LiveData<List<Song>> getSongs() {
        return songs;
    }

    private MutableLiveData<Event> event = new MutableLiveData<>();

    public LiveData<Event> getEvent() {
        return event;
    }

    public LiveData<List<TopList.ListBean>> getTopListLiveData() {
        return topListLiveData;
    }

    private MutableLiveData<List<TopList.ListBean>> topListLiveData = new MutableLiveData<>();

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

    public void getTopList() {
        TopListRepository.getInstance().get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topList -> {
                    topListLiveData.setValue(topList.list);
                }, err -> {
                    err.printStackTrace();
                    ToastUtilKt.toast(getApplication(), "网络连接失败：" + err.getMessage());
                });
    }

    public Observable<TopListSong> getTopListDetails(int index) {
        return TopListRepository.getInstance().getTopListDetails(topListLiveData.getValue().get(index).id)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(data -> {
                    TopList.ListBean listBean = topListLiveData.getValue().get(index);
                    listBean.tracks = data.playlist.tracks;
                });
    }

    public Observable<MusicUrl> getMusicUrls(List<Long> idList){
        return MusicInfoRepository.getInstance().getMusicUrls(idList);
    }

}
