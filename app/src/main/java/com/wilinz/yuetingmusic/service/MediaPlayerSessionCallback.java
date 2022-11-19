package com.wilinz.yuetingmusic.service;

import android.app.Service;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.wilinz.yuetingmusic.data.model.Song;

import java.io.IOException;

public class MediaPlayerSessionCallback extends MyMediaSessionCallback {

    private final static String TAG = "MediaPlayerSession";
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final MediaSessionCompat mediaSession;

    public MediaPlayerSessionCallback(Service service, MyAudioManager myAudioManager, MyNotificationManager myNotificationManager, MediaSessionCompat mediaSession) {
        super(service, myAudioManager, myNotificationManager);
        this.mediaSession = mediaSession;
        initPlayer(mediaSession);
    }

    private void initPlayer(MediaSessionCompat mediaSession) {
        mediaPlayer.setOnPreparedListener(this::onPreparedListener);
        mediaPlayer.setOnCompletionListener(this::onCompletionListener);
    }

    private void onPreparedListener(MediaPlayer player) {
        onPlay();
    }

    private void onCompletionListener(MediaPlayer player) {
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, mediaPlayer.getCurrentPosition(), 1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_NONE))
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
        mediaPlayer.reset();
    }

    public static long getAvailableActions(@PlaybackStateCompat.State int state) {
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
        if (getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING
                && myAudioManager.requestAudioFocus()
        ) {
            handlePlay();
        }
    }

    private void handlePlay() {
        mediaPlayer.start();
        register();
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        mediaPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);

        // 更新视频的总进度, setMetadata 会更新MediaControlCompat的onMetadataChanged
        sendMetadata();
    }

    public void sendMetadata() {
        Song song = PlayQueue.getInstance().getSong();
        if (song != null) {
            mediaSession.setMetadata(song.mapToMediaMetadata(mediaPlayer.getDuration()));
        }
    }

    private PlaybackStateCompat getPlaybackState() {
        return mediaSession.getController().getPlaybackState();
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
//            mediaPlayer.pause();
//            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PAUSED,
//                            mediaPlayer.getCurrentPosition(),
//                            1.0f)
//                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_PAUSED))
//                    .build();
//            mediaSession.setPlaybackState(playbackState);
//            myAudioManager.unregisterBecomingNoisyReceiver();
//            myAudioManager.abandonAudioFocus();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.stop();
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        try {
            PlayQueue.getInstance().setCurrentIndexByUri(uri);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(service, uri);
            PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_CONNECTING,
                            mediaPlayer.getCurrentPosition(),
                            1.0f)
                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_CONNECTING))
                    .build();
            mediaSession.setPlaybackState(mPlaybackStateCompat);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        PlayQueue.getInstance().moveToNext();
        onPlayFromUri(PlayQueue.getInstance().getSong().uri, null);
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        PlayQueue.getInstance().moveToPrevious();
        onPlayFromUri(PlayQueue.getInstance().getSong().uri, null);
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
        mediaPlayer.setLooping(false);
        PlayQueue.getInstance().setShuffleMode(true);
    }

    @Override
    public void unregister() {
        super.unregister();
        mediaPlayer.release();
        mediaSession.release();
    }

}
