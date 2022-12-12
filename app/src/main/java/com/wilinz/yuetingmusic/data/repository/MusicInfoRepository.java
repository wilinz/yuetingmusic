package com.wilinz.yuetingmusic.data.repository;

import com.wilinz.yuetingmusic.data.AppNewWork;
import com.wilinz.yuetingmusic.data.model.MusicUrl;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import kotlin.collections.CollectionsKt;
import retrofit2.http.Query;

public class MusicInfoRepository {

    public Observable<MusicUrl> getMusicUrls(List<Long> idList) {
        String query = String.join(",", CollectionsKt.map(idList, Object::toString));
        return AppNewWork.getInstance().musicInfoService.getMusicUrls(query);
    }

    private static volatile MusicInfoRepository singleton;

    private MusicInfoRepository() {
    }

    public static MusicInfoRepository getInstance() {
        if (singleton == null) {
            synchronized (MusicInfoRepository.class) {
                if (singleton == null) {
                    singleton = new MusicInfoRepository();
                }
            }
        }
        return singleton;
    }
}