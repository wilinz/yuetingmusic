package com.wilinz.yuetingmusic.ui.commen;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.constant.PlayMode;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.player.MusicService;
import com.wilinz.yuetingmusic.player.PlayerManager;
import com.wilinz.yuetingmusic.util.RxTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressLint("LongLogTag")
public class MediaControllerViewModel extends AndroidViewModel {

    private final static String TAG = "MediaControllerViewModel";
    private final MutableLiveData<MediaMetadataCompat> mediaMetadataLiveData = new MutableLiveData<>();
    private final MutableLiveData<PlaybackStateCompat> playStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> playPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> playModeLiveData = new MutableLiveData<>();

    public LiveData<Integer> getPlayModeLiveData() {
        return playModeLiveData;
    }

    public LiveData<MediaMetadataCompat> getMediaMetadataLiveData() {
        return mediaMetadataLiveData;
    }

    public LiveData<Long> getPlayPositionLiveData() {
        return playPositionLiveData;
    }

    public LiveData<PlaybackStateCompat> getPlaybackStateLiveData() {
        return playStateLiveData;
    }

    public PlaybackStateCompat getPlaybackState() {
        return mediaController.getPlaybackState();
    }

    private final MediaBrowserCompat mediaBrowser;

    public MediaControllerCompat getMediaController() {
        return mediaController;
    }

    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback controllerCallback;
    private RxTimer timer;

    public MediaControllerViewModel(@NonNull Application application) {
        super(application);
        playModeLiveData.setValue(Pref.getInstance(application).getPlayMode());
        mediaBrowser = new MediaBrowserCompat(getApplication(),
                new ComponentName(getApplication(), MusicService.class),
                connectionCallbacks,
                null); // optional Bundle
        mediaBrowser.connect();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                    mediaController =
                            new MediaControllerCompat(getApplication(), // Context
                                    token);
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // ??????????????????????????????????????????????????????????????????
                }

                @Override
                public void onConnectionFailed() {
                    // ?????????????????????????????????
                }
            };

    public void playFromUri(List<Song> songs, Song song) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Key.songList, (ArrayList<? extends Parcelable>) songs);
        mediaController.getTransportControls().playFromUri(Uri.parse(song.url), bundle);
    }

    private void buildTransportControls() {
        mediaMetadataLiveData.setValue(mediaController.getMetadata());
        playStateLiveData.setValue(mediaController.getPlaybackState());
        updatePlaybackState(mediaController.getPlaybackState());
        playPositionLiveData.setValue(mediaController.getPlaybackState().getPosition());
        // ???????????????????????????
        controllerCallback =
                new MediaControllerCompat.Callback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        if (metadata == null) return;
                        MediaControllerViewModel.this.mediaMetadataLiveData.setValue(metadata);
                    }

                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {
                        if (state == null) return;
                        MediaControllerViewModel.this.playStateLiveData.setValue(state);
                        updatePlaybackState(state);
                    }
                };
        mediaController.registerCallback(controllerCallback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
        //?????????????????? MediaSession ??????????????????
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
            controllerCallback = null;
        }
        mediaBrowser.disconnect();
    }

    public void pause() {
        mediaController.getTransportControls().pause();
    }

    public void play() {
        mediaController.getTransportControls().play();
    }

    public void seekTo(long pos) {
        mediaController.getTransportControls().seekTo(pos);
    }

    public void skipToPrevious() {
        mediaController.getTransportControls().skipToPrevious();
    }

    public void skipToNext() {
        mediaController.getTransportControls().skipToNext();
    }

    public void switchPlayMode() {
        Integer playModeInteger = getPlayModeLiveData().getValue();
        int playMode = playModeInteger != null ? playModeInteger : PlayMode.ORDERLY;
        PlayerManager.switchPlayMode(mediaController.getTransportControls(), playMode);
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        playStateLiveData.setValue(state);
        Bundle bundle = state.getExtras();
        if (bundle != null) {
            int playMode = state.getExtras().getInt(Key.playMode, PlayMode.UNKNOWN);
            if (playMode != PlayMode.UNKNOWN) {
                playModeLiveData.setValue(playMode);
            }
        }
        if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            if (timer != null) timer.cancel();
        } else {
            timer = new RxTimer();
            timer.interval(1000, (number) -> {
                long position = mediaController.getPlaybackState().getPosition();
                playPositionLiveData.setValue(position);
            });
        }
    }

    private final MutableLiveData<Float> updatePictureRotationLiveData = new MutableLiveData<>();

    public RxTimer pictureRotationTimer;

    public LiveData<Float> getUpdatePictureRotationLiveData() {
        return updatePictureRotationLiveData;
    }

    public void startPictureRotationTimer(float growth) {
        if (pictureRotationTimer == null || pictureRotationTimer.isCanceled()) {
//            stopPictureRotationTimer();
            pictureRotationTimer = new RxTimer();
            pictureRotationTimer.interval(16, count -> {
                float rotation = Optional.ofNullable(updatePictureRotationLiveData.getValue()).orElse(0f);
                updatePictureRotationLiveData.setValue(Math.abs(rotation - (360 - growth)) <= 0.1 ? 0 : rotation + growth);
            });
        }
    }

    public void stopPictureRotationTimer() {
        if (pictureRotationTimer != null) pictureRotationTimer.cancel();
    }

}