package com.wilinz.yuetingmusic.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.widget.Toast
import com.wilinz.yuetingmusic.App
import com.wilinz.yuetingmusic.data.model.FavoriteSong
import com.wilinz.yuetingmusic.data.model.RecentSong
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.event.FavoriteUpdatedEvent
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent
import com.wilinz.yuetingmusic.util.MediaUtil.getMusicList
import com.wilinz.yuetingmusic.util.MediaUtil.saveAudio
import com.wilinz.yuetingmusic.util.toLongArray
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal.beginTransaction
import org.litepal.LitePal.count
import org.litepal.LitePal.delete
import org.litepal.LitePal.endTransaction
import org.litepal.LitePal.findAll
import org.litepal.LitePal.order
import org.litepal.LitePal.select
import org.litepal.LitePal.setTransactionSuccessful
import java.util.*

class SongRepository private constructor() {
    var currentSong: Song? = null
    var playQueue = java.util.List.of<Song>()
    var isInserted: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(App.instance)
            .getBoolean(isInsertedKey, false)
        set(isInserted) {
            PreferenceManager.getDefaultSharedPreferences(App.instance).edit()
                .putBoolean(isInsertedKey, isInserted)
                .apply()
        }

    fun getLocalMusic(context: Context): Observable<List<Song>> {
        return Observable.fromCallable { getMusicList(context) }
            .doOnNext { songs: List<Song> -> Log.d(TAG, "getLocalMusic: $songs") }
            .subscribeOn(Schedulers.io())
    }

    fun getMediaItemList(songs: List<Song>): List<MediaBrowserCompat.MediaItem> {
        return songs.map { obj: Song -> obj.mapToMediaItem() }
    }

    @SuppressLint("CheckResult")
    fun insertAudio(context: Context) {
        Observable.fromAction<Any> {
            val manager = context.assets
            val fileList = manager.list("musics")
            for (filename in fileList!!) {
                val input = manager.open("musics/$filename")
                saveAudio(context, filename, input)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { `object`: Any? -> },
                { e: Throwable ->
                    Toast.makeText(context, "插入音乐到本地媒体库失败：$e", Toast.LENGTH_SHORT).show()
                }
            ) { Toast.makeText(context, "插入音乐到本地媒体库成功，请刷新", Toast.LENGTH_SHORT).show() }
    }

    fun saveRecentSong(song: Song): Observable<Song> {
        return UserRepository.instance!!.activeUser
            .observeOn(Schedulers.io())
            .map { user: Optional<User> ->
                if (user.isPresent) {
                    val count = count(RecentSong::class.java)
                    if (count > 1000) {
                        delete(
                            RecentSong::class.java,
                            order("timestamp").findFirst(RecentSong::class.java).id.toLong()
                        )
                    }
                    val recentSong = RecentSong()
                    recentSong.song = song
                    recentSong.user = user.get()
                    recentSong.uniqueId = song.uniqueId
                    recentSong.timestamp = System.currentTimeMillis()
                    beginTransaction()
                    if (saveSongOrUpdate(song) && recentSong.saveOrUpdate(
                            "uniqueid = ?",
                            song.uniqueId
                        )
                    ) {
                        setTransactionSuccessful()
                    }
                    endTransaction()
                    EventBus.getDefault().post(RecentRecordUpdatedEvent())
                    Log.d(TAG, "onPlayFromUri: 保存成功")
                }
                song
            }
    }

    private fun saveSongOrUpdate(song: Song): Boolean {
        return song.saveOrUpdate("uniqueid = ?", song.uniqueId)
    }

    //                        List<String> idList = CollectionsKt.map(favoriteSongs, favoriteSong -> favoriteSong.song_id + "");
//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
    val favoriteList: Observable<List<Song>>
        get() = UserRepository.instance!!.activeUser
            .map { user: Optional<User> ->
                if (user.isPresent) {
                    val favoriteSongs =
                        select("song_id").where("user_id = ?", user.get().id.toString() + "")
                            .order("timestamp desc").find(
                                FavoriteSong::class.java
                            )
                    //                        List<String> idList = CollectionsKt.map(favoriteSongs, favoriteSong -> favoriteSong.song_id + "");
//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
                    val idList =
                        toLongArray(favoriteSongs.map { favoriteSong: FavoriteSong -> favoriteSong.song_id })

//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
                    val songs =
                        findAll(Song::class.java, *idList)
                    return@map favoriteSongs.mapNotNull { favoriteSong: FavoriteSong -> songs.firstOrNull { song: Song -> song.id.toLong() == favoriteSong.song_id } }
                }
                java.util.List.of()
            }

    fun getRecentList(limit: Int): Observable<List<Song>> {
        return UserRepository.instance!!.activeUser
            .map { user: Optional<User> ->
                if (user.isPresent) {
                    var fluentQuery =
                        select("song_id").where("user_id = ?", user.get().id.toString() + "")
                            .order("timestamp desc")
                    if (limit > 0) fluentQuery = fluentQuery.limit(limit)
                    val recentSongs = fluentQuery.find(
                        RecentSong::class.java
                    )
                    val idList =
                        toLongArray(recentSongs.map { recentSong: RecentSong -> recentSong.song_id })

//                        String idListString = String.join(",", idList);
//                        List<Song> songs = LitePal.where(String.format("id in (%s)", idListString)).find(Song.class);
                    val songs = findAll(Song::class.java, *idList)
                    return@map recentSongs.mapNotNull { recentSong: RecentSong -> songs.firstOrNull { song: Song -> song.id.toLong() == recentSong.song_id } }
                }
                java.util.List.of()
            }
    }

    fun saveFavoriteSong(song: Song?): Observable<Song> {
        return UserRepository.instance!!.activeUser
            .map { user: Optional<User> ->
                if (song == null) throw Exception("song is null")
                if (user.isPresent) {
                    val favoriteSong = FavoriteSong()
                    favoriteSong.song = song
                    favoriteSong.user = user.get()
                    favoriteSong.uniqueId = song.uniqueId
                    favoriteSong.timestamp = System.currentTimeMillis()
                    favoriteSong.save()
                    beginTransaction()
                    saveSongOrUpdate(song)
                    setTransactionSuccessful()
                    endTransaction()
                    EventBus.getDefault().post(FavoriteUpdatedEvent())
                    Log.d(TAG, "saveFavoriteSong: 保存成功")
                }
                song
            }
    }

    companion object {
        private const val TAG = "SongRepository"
        private const val isInsertedKey = "isInserted"

        @Volatile
        private var singleton: SongRepository? = null
        @JvmStatic
        val instance: SongRepository?
            get() {
                if (singleton == null) {
                    synchronized(SongRepository::class.java) {
                        if (singleton == null) {
                            singleton = SongRepository()
                        }
                    }
                }
                return singleton
            }
    }
}