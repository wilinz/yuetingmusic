package com.wilinz.yuetingmusic.data.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.App;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.util.MediaUtil;

import java.io.InputStream;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.collections.CollectionsKt;

public class SongRepository {

    private static final String isInsertedKey = "isInserted";

    private static List<Song> songs = null;

    public static boolean isInserted() {
        return PreferenceManager.getDefaultSharedPreferences(App.instance).getBoolean(isInsertedKey, false);
    }

    public static void setIsInserted(boolean isInserted) {
        PreferenceManager.getDefaultSharedPreferences(App.instance).edit()
                .putBoolean(isInsertedKey, isInserted)
                .apply();
    }

    public static Observable<List<Song>> getMusics(@NonNull Context context) {
        return Observable.fromCallable(() -> {
                    if (songs == null) songs = MediaUtil.getMusicList(context);
                    return songs;
                })
                .map((songs) -> {
                    if (songs.size() < 10 && !isInserted()) {
                        insertAudio(context);
                        SongRepository.setIsInserted(true);
                        return MediaUtil.getMusicList(context);
                    }
                    return songs;
                })
                .subscribeOn(Schedulers.io());
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItemList(List<Song> songs) {
        return CollectionsKt.map(songs, Song::mapToMediaItem);
    }

    public static void insertAudio(Context context) {
        Observable.fromAction(() -> {
                    AssetManager manager = context.getAssets();
                    String[] fileList = manager.list("musics");
                    for (String filename : fileList) {
                        InputStream input = manager.open("musics" + "/" + filename);
                        MediaUtil.saveAudio(context, filename, input);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (object) -> {
                        },
                        (e -> {
                            Toast.makeText(context, "插入音乐到本地媒体库失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                        }),
                        () -> {
                            Toast.makeText(context, "插入音乐到本地媒体库成功，请刷新", Toast.LENGTH_SHORT).show();
                        });
    }

}
