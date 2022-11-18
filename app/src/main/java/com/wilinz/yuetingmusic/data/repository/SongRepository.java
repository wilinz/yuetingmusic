package com.wilinz.yuetingmusic.data.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.App;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.util.MediaUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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

//    public static List<MediaMetadataCompat> mapToMetadata(List<Song> songs){
//        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
//        for (Song song : songs) {
//            MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "" + song.path)
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "采蘑菇的小姑娘")
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "群星")
//                    .build();
//
//        }
//
//
//        mediaItems.add()
//    }

    public static MediaMetadataCompat transformPlayBeanByDuration(Song song,long duration) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "" + song.path)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.song)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.singer)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build();
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItem(List<Song> songs) {

        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>(songs.size());
        for (Song song : songs) {
            MediaDescriptionCompat desc =
                    new MediaDescriptionCompat.Builder()
                            .setMediaId(song.path)
                            .setTitle(song.song)
                            .setSubtitle(song.singer)
                            .build();

            MediaBrowserCompat.MediaItem songList =
                    new MediaBrowserCompat.MediaItem(desc,
                            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

            mediaItems.add(songList);
        }
        return mediaItems;

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
