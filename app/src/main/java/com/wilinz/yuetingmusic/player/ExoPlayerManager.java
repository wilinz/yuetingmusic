package com.wilinz.yuetingmusic.player;

import android.app.Service;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ShuffleOrder;
import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.constant.PlayMode;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.util.LogUtil;
import com.wilinz.yuetingmusic.util.MediaUtil;

import java.util.List;

import kotlin.collections.CollectionsKt;

public class ExoPlayerManager extends MyMediaSessionCallback {

    private final static String TAG = "MediaPlayerSession";
    //    private final MediaPlayer mediaPlayer = new MediaPlayer();
    ExoPlayer exoPlayer = new ExoPlayer.Builder(service).build();

    private final MediaSessionCompat mediaSession;

    private boolean isPreparedSeek = false;
    private long preparedSeekPosition = 0;

    public List<Song> getPlayQueue() {
        return playQueue;
    }

    private Song currentSong;

    private List<Song> playQueue = List.of();

    public ExoPlayerManager(Service service, MyAudioManager myAudioManager, MyNotificationManager myNotificationManager, MediaSessionCompat mediaSession) {
        super(service, myAudioManager, myNotificationManager);
        this.mediaSession = mediaSession;
        initPlayer(mediaSession);
    }

    private void initPlayer(MediaSessionCompat mediaSession) {
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) sendMetadata();
            }

        });
//        mediaPlayer.setOnPreparedListener(this::onPreparedListener);
//        mediaPlayer.setOnCompletionListener(this::onCompletionListener);
    }

    private void onPreparedListener(MediaPlayer player) {
        onPlay();
        if (isPreparedSeek) {
            isPreparedSeek = false;
            onSeekTo(preparedSeekPosition);
        }
    }

    private void onCompletionListener(MediaPlayer player) {
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, exoPlayer.getCurrentPosition(), 1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_NONE))
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
//        mediaPlayer.reset();
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
            exoPlayer.play();
            register();
            // 更新视频的总进度, setMetadata 会更新MediaControlCompat的onMetadataChanged
            PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING,
                            exoPlayer.getCurrentPosition(),
                            1.0f)
                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                    .build();
            mediaSession.setPlaybackState(mPlaybackStateCompat);
        }
    }

    public void sendMetadata() {
        MediaItem mediaItem = exoPlayer.getCurrentMediaItem();
        MediaMetadataCompat mediaMetadataCompat = null;
        if (mediaItem == null) {
            if (currentSong != null) {
                mediaMetadataCompat = currentSong.mapToMediaMetadata(exoPlayer.getCurrentPosition());
            }
        } else {
            mediaMetadataCompat = MediaUtil.getMediaMetadataCompat(mediaItem, exoPlayer.getDuration());
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
            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED,
                            exoPlayer.getCurrentPosition(),
                            1.0f)
                    .setActions(getAvailableActions(PlaybackStateCompat.STATE_PAUSED))
                    .build();
            mediaSession.setPlaybackState(playbackState);
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
//            PlayQueue.getInstance().setCurrentIndexByUri(uri);
//        List<Song> songs
//               new  MediaItem.Builder()
//                       .
        List<Song> songs = extras.getParcelableArrayList(Key.songList);
        if (songs != null) {
            int index = CollectionsKt.indexOfFirst(songs, song -> song.uri.equals(uri));
            playQueue = songs;
            currentSong = songs.get(index);
            List<MediaItem> mediaItems = CollectionsKt.map(songs, Song::mapToExoPlayerMediaItem);
            exoPlayer.setMediaItems(mediaItems, index, 0);
        } else {
            exoPlayer.setMediaItem(MediaItem.fromUri(uri));
        }

        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_CONNECTING,
                        exoPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_CONNECTING))
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
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
//        public static final int SEEK_PREVIOUS_SYNC    = 0x00; //同步播放模式，会往前一点播放，默认模式
//        public static final int SEEK_NEXT_SYNC        = 0x01; //同步播放模式，会后一点播放
//        public static final int SEEK_CLOSEST_SYNC     = 0x02; //同步播放模式，精确播放
//        public static final int SEEK_CLOSEST          = 0x03; //异步播放模式，精确播放

//        if (getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                mediaPlayer.seekTo(pos, MediaPlayer.SEEK_CLOSEST);
//            } else {
//                mediaPlayer.seekTo((int) pos);
//            }

//        } else {
//            isPreparedSeek = true;
//            preparedSeekPosition = pos;
//        }
        exoPlayer.seekTo(pos);
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        exoPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
    }

    @Override
    public void onSetPlaybackSpeed(float speed) {
        super.onSetPlaybackSpeed(speed);
    }

    @Override
    public void onSetRepeatMode(int repeatMode) {
        super.onSetRepeatMode(repeatMode);
        int playModel = 0;
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            playModel = PlayMode.SINGLE_LOOP;
        } else {
            playModel = PlayMode.ORDERLY;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(Key.playMode, playModel);
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        exoPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                .setExtras(bundle)
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
        exoPlayer.setRepeatMode(repeatMode);
    }

    @Override
    public void onSetShuffleMode(int shuffleMode) {
        super.onSetShuffleMode(shuffleMode);
        Bundle bundle = new Bundle();
        bundle.putInt(Key.playMode, PlayMode.SHUFFLE);
        PlaybackStateCompat mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        exoPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(getAvailableActions(PlaybackStateCompat.STATE_PLAYING))
                .setExtras(bundle)
                .build();
        mediaSession.setPlaybackState(mPlaybackStateCompat);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        exoPlayer.setShuffleOrder(new ShuffleOrder.DefaultShuffleOrder(exoPlayer.getMediaItemCount()));
    }

    @Override
    public void unregister() {
        super.unregister();
        exoPlayer.release();
        mediaSession.release();
    }

}
