package com.wilinz.yuetingmusic.service;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.wilinz.yuetingmusic.data.repository.SongRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String TAG = "MusicService";

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MyAudioManager myAudioManager;
    private MyNotificationManager myNotificationManager;
    private MediaPlayerSessionCallback mediaPlayerSessionCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, "MediaSessionCompat");
        // 启用来自 MediaButtons 和 TransportControls 的回调
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // 使用 ACTION_PLAY 设置初始 PlaybackState，以便媒体按钮可以启动播放器
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        myAudioManager = new MyAudioManagerImpl(this, mediaSession);
        myNotificationManager = new MyNotificationManager(this, mediaSession);

        mediaPlayerSessionCallback=new MediaPlayerSessionCallback(this, myAudioManager, myNotificationManager, mediaSession);
        // MySessionCallback() 具有处理来自媒体控制器的回调的方法
        mediaSession.setCallback(mediaPlayerSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        //（可选）控制指定包名称的访问级别。您需要编写自己的逻辑来执行此操作。返回一个根 ID，客户端可以使用它与 onLoadChildren() 一起检索内容层次结构。
        return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        // 不允许浏览
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }
        // 例如假设音乐目录已经加载缓存。
        SongRepository.getMusics(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((songs -> {
                    result.sendResult(SongRepository.getMediaItem(songs));
                }));
        result.detach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayerSessionCallback.unregister();
    }
}