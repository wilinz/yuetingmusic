package com.wilinz.yuetingmusic.data.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.App;
import com.wilinz.yuetingmusic.data.model.FavoriteSong;
import com.wilinz.yuetingmusic.data.model.RecentSong;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent;
import com.wilinz.yuetingmusic.event.FavoriteUpdatedEvent;
import com.wilinz.yuetingmusic.util.ArrayUtilKt;
import com.wilinz.yuetingmusic.util.MediaUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.FluentQuery;
import org.litepal.LitePal;

import java.io.InputStream;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.collections.CollectionsKt;

public class SongRepository {

    private Song currentSong;
    private List<Song> playQueue = List.of();

    private final static String TAG = "SongRepository";
    private final static String isInsertedKey = "isInserted";

    public boolean isInserted() {
        return PreferenceManager.getDefaultSharedPreferences(App.instance).getBoolean(isInsertedKey, false);
    }

    public void setIsInserted(boolean isInserted) {
        PreferenceManager.getDefaultSharedPreferences(App.instance).edit()
                .putBoolean(isInsertedKey, isInserted)
                .apply();
    }

    public Observable<List<Song>> getLocalMusic(@NonNull Context context) {
        return Observable.fromCallable(() -> MediaUtil.getMusicList(context))
                .doOnNext(songs -> {
                    Log.d(TAG, "getLocalMusic: " + songs);
                })
                .subscribeOn(Schedulers.io());
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItemList(List<Song> songs) {
        return CollectionsKt.map(songs, Song::mapToMediaItem);
    }

    public void insertAudio(Context context) {
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

    public Observable<Song> saveRecentSong(Song song) {
        return UserRepository.getInstance().getActiveUser()
                .observeOn(Schedulers.io())
                .map(user -> {
                    if (user.isPresent()) {
                        int count = LitePal.count(RecentSong.class);
                        if (count > 1000) {
                            LitePal.delete(RecentSong.class, LitePal.order("timestamp").findFirst(RecentSong.class).id);
                        }
                        RecentSong recentSong = new RecentSong();
                        recentSong.song = song;
                        recentSong.user = user.get();
                        recentSong.uniqueId = song.getUniqueId();
                        recentSong.timestamp = System.currentTimeMillis();
                        LitePal.beginTransaction();
                        if (saveSongOrUpdate(song) && recentSong.saveOrUpdate("uniqueid = ?", song.getUniqueId())) {
                            LitePal.setTransactionSuccessful();
                        }
                        LitePal.endTransaction();
                        EventBus.getDefault().post(new RecentRecordUpdatedEvent());
                        Log.d(TAG, "onPlayFromUri: 保存成功");
                    }
                    return song;
                });
    }

    private boolean saveSongOrUpdate(Song song) {
        return song.saveOrUpdate("uniqueid = ?", song.getUniqueId());
    }

    public Observable<List<Song>> getFavoriteList() {
        return UserRepository.getInstance().getActiveUser()
                .map(user -> {
                    if (user.isPresent()) {
                        List<FavoriteSong> favoriteSongs = LitePal.select("song_id").where("user_id = ?", user.get().id + "").order("timestamp desc").find(FavoriteSong.class);
//                        List<String> idList = CollectionsKt.map(favoriteSongs, favoriteSong -> favoriteSong.song_id + "");
//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
                        long[] idList = ArrayUtilKt.toLongArray(CollectionsKt.map(favoriteSongs, favoriteSong -> favoriteSong.song_id));

//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
                        List<Song> songs = LitePal.findAll(Song.class, idList);
                        List<Song> songs1 = CollectionsKt.mapNotNull(favoriteSongs, favoriteSong -> CollectionsKt.firstOrNull(songs, song -> song.id == favoriteSong.song_id));
                        return songs1;
                    }
                    return List.of();
                });
    }

    public Observable<List<Song>> getRecentList(int limit) {
        return UserRepository.getInstance().getActiveUser()
                .map(user -> {
                    if (user.isPresent()) {
                        FluentQuery fluentQuery = LitePal.select("song_id").where("user_id = ?", user.get().id + "").order("timestamp desc");
                        if (limit > 0) fluentQuery = fluentQuery.limit(limit);
                        List<RecentSong> recentSongs = fluentQuery.find(RecentSong.class);
                        long[] idList = ArrayUtilKt.toLongArray(CollectionsKt.map(recentSongs, recentSong -> recentSong.song_id));

//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
                        List<Song> songs = LitePal.findAll(Song.class, idList);
                        List<Song> songs1 = CollectionsKt.mapNotNull(recentSongs, recentSong -> CollectionsKt.firstOrNull(songs, song -> song.id == recentSong.song_id));
                        return songs1;
                    }
                    return List.of();
                });
    }

    public Observable<Song> saveFavoriteSong(Song song) {
        return UserRepository.getInstance().getActiveUser()
                .map(user -> {
                    if (song == null) throw new Exception("song is null");
                    if (user.isPresent()) {
                        FavoriteSong favoriteSong = new FavoriteSong();
                        favoriteSong.song = song;
                        favoriteSong.user = user.get();
                        favoriteSong.uniqueId = song.getUniqueId();
                        favoriteSong.timestamp = System.currentTimeMillis();
                        favoriteSong.save();
                        LitePal.beginTransaction();
                        saveSongOrUpdate(song);
                        LitePal.setTransactionSuccessful();
                        LitePal.endTransaction();
                        EventBus.getDefault().post(new FavoriteUpdatedEvent());
                        Log.d(TAG, "saveFavoriteSong: 保存成功");
                    }
                    return song;
                });
    }

    private static volatile SongRepository singleton;

    private SongRepository() {
    }

    public static SongRepository getInstance() {
        if (singleton == null) {
            synchronized (SongRepository.class) {
                if (singleton == null) {
                    singleton = new SongRepository();
                }
            }
        }
        return singleton;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public List<Song> getPlayQueue() {
        return playQueue;
    }

    public void setPlayQueue(List<Song> playQueue) {
        this.playQueue = playQueue;
    }
}
