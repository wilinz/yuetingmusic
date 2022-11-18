package com.wilinz.yuetingmusic.service;

import android.app.Service;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class MediaPlayerSessionCallback extends MyMediaSessionCallback {

    private final static String TAG = "MediaPlayerSession";
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final MediaSessionCompat mediaSession;

    public MediaPlayerSessionCallback(Service service, MyAudioManager myAudioManager, MyNotificationManager myNotificationManager, MediaSessionCompat mediaSession) {
        super(service, myAudioManager, myNotificationManager);
        this.mediaSession = mediaSession;
        mediaPlayer.setOnPreparedListener((player) -> {
            mediaPlayer.start();
            PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer.getCurrentPosition(),
                            1.0f)
                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                    .build();
            mediaSession.setPlaybackState(mPlaybackStateCompat);
        });
        mediaPlayer.setOnCompletionListener((player) -> {
            PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, mediaPlayer.getCurrentPosition(), 1.0f)
                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_NONE))
                    .build();
            mediaSession.setPlaybackState(mPlaybackStateCompat);
            mediaPlayer.reset();
        });
    }

    public long getAvailableActions(@PlaybackStateCompat.State int state) {
        long actions = (PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_REWIND
                | PlaybackStateCompat.ACTION_FAST_FORWARD);

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            actions = actions | PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions = actions | PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    @Override
    public void onPlay() {
        super.onPlay();
        PlaybackStateCompat playbackStateCompat=mediaSession.getController().getPlaybackState();
        if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED
                && myAudioManager.requestAudioFocus()
        ) {
            mediaPlayer.start();
            PlaybackStateCompat  mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer.getCurrentPosition(),
                            1.0f)
                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                    .build();
            mediaSession.setPlaybackState(mPlaybackStateCompat);

            // 更新视频的总进度, setMetadata 会更新MediaControlCompat的onMetadataChanged
            mediaSession.setMetadata(LocalDataHelper.transformPlayBeanByDuration(getPlayBean(),
                    mMediaPlayer.duration.toLong()));
            "mMediaPlayer.getDuration()=${mMediaPlayer.duration}".logd()
        }
        mediaPlayer.start();
//        mediaSession.setPlaybackState(new PlaybackStateCompat(PlaybackStateCompat.STATE_PLAYING,mediaPlayer.getCurrentPosition(),0,0,0,0,"",System.currentTimeMillis(), List.of(),0,null));
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.stop();
    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {
        super.onPrepareFromUri(uri, extras);
        Log.d(TAG, "onPrepareFromUri: ");
        try {
            mediaPlayer.setDataSource(service, uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mediaPlayer.setOnPreparedListener((player)->{
//
//        });
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        onPrepareFromUri(uri, extras);
        onPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
//        public static final int SEEK_PREVIOUS_SYNC    = 0x00; //同步播放模式，会往前一点播放，默认模式
//        public static final int SEEK_NEXT_SYNC        = 0x01; //同步播放模式，会后一点播放
//        public static final int SEEK_CLOSEST_SYNC     = 0x02; //同步播放模式，精确播放
//        public static final int SEEK_CLOSEST          = 0x03; //异步播放模式，精确播放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(pos, MediaPlayer.SEEK_CLOSEST);
        } else {
            mediaPlayer.seekTo((int) pos);
        }
    }

    @Override
    public void onSetPlaybackSpeed(float speed) {
        super.onSetPlaybackSpeed(speed);
    }

    @Override
    public void onSetRepeatMode(int repeatMode) {
        super.onSetRepeatMode(repeatMode);
        mediaPlayer.setLooping(true);
    }

    @Override
    public void onSetShuffleMode(int shuffleMode) {
        super.onSetShuffleMode(shuffleMode);
    }
}
