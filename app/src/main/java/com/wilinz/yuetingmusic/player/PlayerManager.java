package com.wilinz.yuetingmusic.player;

import android.app.Service;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.constant.PlayMode;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.util.LogUtil;
import com.wilinz.yuetingmusic.util.MediaUtil;
import com.wilinz.yuetingmusic.util.ToastUtilKt;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import kotlin.collections.CollectionsKt;

public class PlayerManager extends MyMediaSessionCallback {

    private final static String TAG = "MediaPlayerSession";
    //    private final MediaPlayer mediaPlayer = new MediaPlayer();
    ExoPlayer exoPlayer = new ExoPlayer.Builder(service).build();

    private final MediaSessionCompat mediaSession;

    private boolean isPreparedSeek = false;
    private long preparedSeekPosition = 0;

    public PlayerManager(Service service, MyAudioManager myAudioManager, MyNotificationManager myNotificationManager, MediaSessionCompat mediaSession) {
        super(service, myAudioManager, myNotificationManager);
        this.mediaSession = mediaSession;
        initPlayer(mediaSession);
    }

    private void initPlayer(MediaSessionCompat mediaSession) {
        setPlayMode(Pref.getInstance(service.getApplication()).getPlayMode());
        exoPlayer.addListener(new Player.Listener() {

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                if (mediaItem == null || mediaItem.localConfiguration == null) return;
                Song song = CollectionsKt.firstOrNull(SongRepository.getInstance().getPlayQueue(), song1 -> song1.getUniqueId().equals(mediaItem.mediaId));
                SongRepository.getInstance().setCurrentSong(song);
                SongRepository.getInstance().saveRecentSong(song)
                        .subscribe();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    sendMetadata();
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                }
            }

            @Override
            public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
                sendMetadata();
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                error.printStackTrace();
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
//                exoPlayer.setMediaItem(exoPlayer.getCurrentMediaItem(), exoPlayer.getCurrentPosition());
//                exoPlayer.prepare();
//                onPlay();
                Log.d(TAG, "onPlayerError: ");
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                updatePlaybackState();
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                updatePlaybackState();
            }

        });
//        mediaPlayer.setOnPreparedListener(this::onPreparedListener);
//        mediaPlayer.setOnCompletionListener(this::onCompletionListener);
    }

    private void setPlayMode(int playMode) {
        switch (playMode) {
            case PlayMode.SINGLE_LOOP:
                exoPlayer.setShuffleModeEnabled(false);
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                break;
            case PlayMode.ORDERLY:
                exoPlayer.setShuffleModeEnabled(false);
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
                break;
            case PlayMode.SHUFFLE:
                exoPlayer.setShuffleModeEnabled(true);
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        }
        Bundle bundle = new Bundle();
        bundle.putInt(Key.playMode, playMode);
        updatePlaybackState(bundle);
    }

    private void updatePlaybackState(Bundle... bundle) {
        updatePlaybackState(exoPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, bundle);
    }

    private void updatePlaybackState(@PlaybackStateCompat.State int state, Bundle... bundle) {
        // 更新视频的总进度, setMetadata 会更新MediaControlCompat的onMetadataChanged
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(state,
                        exoPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(getAvailableActions(state))
                .setExtras(CollectionsKt.getOrNull(Arrays.asList(bundle), 0))
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
    }

    public static long getAvailableActions(@PlaybackStateCompat.State int state) {
        long actions = (PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_REWIND
                | PlaybackStateCompat.ACTION_SEEK_TO);

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
            register();
            exoPlayer.play();
        }
    }

    public void sendMetadata() {
        MediaItem mediaItem = exoPlayer.getCurrentMediaItem();
        MediaMetadataCompat mediaMetadataCompat = null;
        if (mediaItem == null) {
            Song currentSong = SongRepository.getInstance().getCurrentSong();
            if (currentSong != null) {
                mediaMetadataCompat = currentSong.mapToMediaMetadata(exoPlayer.getCurrentPosition(), exoPlayer.getDuration());
            }
        } else {
            mediaMetadataCompat = MediaUtil.getMediaMetadataCompat(mediaItem, exoPlayer.getContentDuration());
        }
        if (mediaMetadataCompat != null) {
            mediaSession.setMetadata(mediaMetadataCompat);
        }
    }

    private PlaybackStateCompat getPlaybackState() {
        return mediaSession.getController().getPlaybackState();
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
        if (getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            exoPlayer.pause();
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
            myAudioManager.unregisterBecomingNoisyReceiver();
            myAudioManager.abandonAudioFocus();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        exoPlayer.stop();
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        List<Song> songs = extras.getParcelableArrayList(Key.songList);
        if (songs != null) {
            int index = CollectionsKt.indexOfFirst(songs, song -> song.url.equals(uri.toString()));
            SongRepository.getInstance().setPlayQueue(songs);
            List<MediaItem> mediaItems = CollectionsKt.map(songs, Song::mapToExoPlayerMediaItem);
            exoPlayer.setMediaItems(mediaItems, index, 0);
        } else {
            exoPlayer.setMediaItem(MediaItem.fromUri(uri));
        }

        updatePlaybackState(PlaybackStateCompat.STATE_CONNECTING);
        exoPlayer.prepare();
        onPlay();
    }


    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        exoPlayer.seekToNext();
        sendMetadata();
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        exoPlayer.seekToPrevious();
        sendMetadata();
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        Log.d(TAG, "onSeekTo: " + pos);
        exoPlayer.seekTo(pos);
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
    }

    @Override
    public void onSetPlaybackSpeed(float speed) {
        super.onSetPlaybackSpeed(speed);
    }

    @Override
    public void onSetRepeatMode(int repeatMode) {
        super.onSetRepeatMode(repeatMode);
        int playModel = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE ? PlayMode.SINGLE_LOOP : PlayMode.ORDERLY;
        setPlayMode(playModel);
        Pref.getInstance(service.getApplication()).setPlayMode(playModel);
    }

    @Override
    public void onSetShuffleMode(int shuffleMode) {
        super.onSetShuffleMode(shuffleMode);
        setPlayMode(PlayMode.SHUFFLE);
        Pref.getInstance(service.getApplication()).setPlayMode(PlayMode.SHUFFLE);
    }

    @Override
    public void unregister() {
        super.unregister();
        exoPlayer.release();
        mediaSession.release();
    }

    @SuppressWarnings("all")
    @Override
    public void onCustomAction(String action, Bundle extras) {
        super.onCustomAction(action, extras);
        switch (action) {
            case ACTION_SAVE_CURRENT_SONG_TO_FAVORITE:
                MediaItem mediaItem = exoPlayer.getCurrentMediaItem();
                if (mediaItem == null || mediaItem.localConfiguration == null) return;
                Song song = CollectionsKt.firstOrNull(SongRepository.getInstance().getPlayQueue(), song1 -> song1.getUniqueId().equals(mediaItem.mediaId));
                SongRepository.getInstance().saveFavoriteSong(SongRepository.getInstance().getCurrentSong())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((song1) -> {
                            ToastUtilKt.toast(service, "收藏成功");
                        }, e -> {
                            e.printStackTrace();
                            ToastUtilKt.toast(service, "收藏失败：" + e.getMessage());
                        });
                break;
        }
    }

    public final static String ACTION_SAVE_CURRENT_SONG_TO_FAVORITE = "action_save_to_favorite";

    public static void switchPlayMode(MediaControllerCompat.TransportControls transportControls, int playMode) {
        if (playMode == PlayMode.ORDERLY) {
            transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
        } else if (playMode == PlayMode.SINGLE_LOOP) {
            transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
        } else {
            transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
        }
    }

}
