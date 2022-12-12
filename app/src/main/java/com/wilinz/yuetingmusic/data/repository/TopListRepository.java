package com.wilinz.yuetingmusic.data.repository;

import com.wilinz.yuetingmusic.data.AppNewWork;
import com.wilinz.yuetingmusic.data.model.TopList;
import com.wilinz.yuetingmusic.data.model.TopListSong;

import io.reactivex.rxjava3.core.Observable;

public class TopListRepository {

    public Observable<TopList> get(){
        return AppNewWork.getInstance().topListService.topList();
    }

    public Observable<TopListSong> getTopListDetails(long id){
        return AppNewWork.getInstance().topListService.getTopListDetails(id);
    }

    private static volatile TopListRepository singleton;

    private TopListRepository() {
    }

    public static TopListRepository getInstance() {
        if (singleton == null) {
            synchronized (MusicInfoRepository.class) {
                if (singleton == null) {
                    singleton = new TopListRepository();
                }
            }
        }
        return singleton;
    }
}