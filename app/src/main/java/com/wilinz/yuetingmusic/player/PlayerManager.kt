package com.wilinz.yuetingmusic.player

import android.app.Service
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.PositionInfo
import com.wilinz.yuetingmusic.Key
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.constant.PlayMode
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.repository.SongRepository.Companion.instance
import com.wilinz.yuetingmusic.util.LogUtil.d
import com.wilinz.yuetingmusic.util.MediaUtil.getMediaMetadataCompat
import com.wilinz.yuetingmusic.util.toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*

class PlayerManager(
    service: Service?,
    myAudioManager: MyAudioManager?,
    myNotificationManager: MyNotificationManager?,
    private val mediaSession: MediaSessionCompat
) : MyMediaSessionCallback(
    service!!, myAudioManager!!, myNotificationManager!!
) {
    //    private final MediaPlayer mediaPlayer = new MediaPlayer();
    var exoPlayer = ExoPlayer.Builder(service!!).build()
    private val isPreparedSeek = false
    private val preparedSeekPosition: Long = 0
    private fun initPlayer(mediaSession: MediaSessionCompat) {
        setPlayMode(getInstance(service.application)!!.playMode)
        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                if (mediaItem == null || mediaItem.localConfiguration == null) return
                val song =
                    instance!!.playQueue.firstOrNull { song1: Song -> song1.uniqueId == mediaItem.mediaId }
                instance!!.currentSong = song
                instance!!.saveRecentSong(song!!)
                    .subscribe()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    sendMetadata()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                sendMetadata()
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                error.printStackTrace()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                //                exoPlayer.setMediaItem(exoPlayer.getCurrentMediaItem(), exoPlayer.getCurrentPosition());
//                exoPlayer.prepare();
//                onPlay();
                Log.d(TAG, "onPlayerError: ")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                updatePlaybackState()
            }

            override fun onPositionDiscontinuity(
                oldPosition: PositionInfo,
                newPosition: PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                updatePlaybackState()
            }
        })
        //        mediaPlayer.setOnPreparedListener(this::onPreparedListener);
//        mediaPlayer.setOnCompletionListener(this::onCompletionListener);
    }

    private fun setPlayMode(playMode: Int) {
        when (playMode) {
            PlayMode.SINGLE_LOOP -> {
                exoPlayer.shuffleModeEnabled = false
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            }
            PlayMode.ORDERLY -> {
                exoPlayer.shuffleModeEnabled = false
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            }
            PlayMode.SHUFFLE -> {
                exoPlayer.shuffleModeEnabled = true
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            }
        }
        val bundle = Bundle()
        bundle.putInt(Key.playMode, playMode)
        updatePlaybackState(bundle)
    }

    private fun updatePlaybackState(vararg bundle: Bundle) {
        updatePlaybackState(
            if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            *bundle
        )
    }

    private fun updatePlaybackState(@PlaybackStateCompat.State state: Int, vararg bundle: Bundle) {
        // 更新视频的总进度, setMetadata 会更新MediaControlCompat的onMetadataChanged
        val mPlaybackStateCompat = PlaybackStateCompat.Builder()
            .setState(
                state,
                exoPlayer.currentPosition,
                1.0f
            )
            .setActions(getAvailableActions(state))
            .setExtras(Arrays.asList(*bundle).getOrNull(0))
            .build()
        mediaSession.setPlaybackState(mPlaybackStateCompat)
    }

    override fun onPlay() {
        super.onPlay()
        if (playbackState.state != PlaybackStateCompat.STATE_PLAYING
            && myAudioManager.requestAudioFocus()
        ) {
            register()
            exoPlayer.play()
        }
    }

    fun sendMetadata() {
        val mediaItem = exoPlayer.currentMediaItem
        var mediaMetadataCompat: MediaMetadataCompat? = null
        if (mediaItem == null) {
            val currentSong = instance!!.currentSong
            if (currentSong != null) {
                mediaMetadataCompat =
                    currentSong.mapToMediaMetadata(exoPlayer.currentPosition, exoPlayer.duration)
            }
        } else {
            mediaMetadataCompat = getMediaMetadataCompat(mediaItem, exoPlayer.contentDuration)
        }
        if (mediaMetadataCompat != null) {
            mediaSession.setMetadata(mediaMetadataCompat)
        }
    }

    private val playbackState: PlaybackStateCompat
        private get() = mediaSession.controller.playbackState

    override fun onPause() {
        super.onPause()
        d(TAG, "onPause")
        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            exoPlayer.pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            myAudioManager.unregisterBecomingNoisyReceiver()
            myAudioManager.abandonAudioFocus()
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.stop()
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle) {
        super.onPlayFromUri(uri, extras)
        val songs: List<Song>? = extras.getParcelableArrayList(Key.songList)
        if (songs != null) {
            val index = songs.indexOfFirst { song: Song -> song.url == uri.toString() }
            instance!!.playQueue = songs
            val mediaItems = songs.map { obj: Song -> obj.mapToExoPlayerMediaItem() }
            exoPlayer.setMediaItems(mediaItems, index, 0)
        } else {
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        }
        updatePlaybackState(PlaybackStateCompat.STATE_CONNECTING)
        exoPlayer.prepare()
        onPlay()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        exoPlayer.seekToNext()
        sendMetadata()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        exoPlayer.seekToPrevious()
        sendMetadata()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        Log.d(TAG, "onSeekTo: $pos")
        exoPlayer.seekTo(pos)
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onSetPlaybackSpeed(speed: Float) {
        super.onSetPlaybackSpeed(speed)
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val playModel =
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) PlayMode.SINGLE_LOOP else PlayMode.ORDERLY
        setPlayMode(playModel)
        getInstance(service.application)!!.playMode = playModel
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        super.onSetShuffleMode(shuffleMode)
        setPlayMode(PlayMode.SHUFFLE)
        getInstance(service.application)!!.playMode = PlayMode.SHUFFLE
    }

    override fun unregister() {
        super.unregister()
        exoPlayer.release()
        mediaSession.release()
    }

    override fun onCustomAction(action: String, extras: Bundle) {
        super.onCustomAction(action, extras)
        when (action) {
            ACTION_SAVE_CURRENT_SONG_TO_FAVORITE -> {
                val mediaItem = exoPlayer.currentMediaItem
                if (mediaItem == null || mediaItem.localConfiguration == null) return
                val song =
                    instance!!.playQueue.firstOrNull { song1: Song -> song1.uniqueId == mediaItem.mediaId }
                instance!!.saveFavoriteSong(instance!!.currentSong)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ song1: Song? -> toast(service, "收藏成功") }) { e: Throwable ->
                        e.printStackTrace()
                        toast(service, "收藏失败：" + e.message)
                    }
            }
        }
    }

    init {
        initPlayer(mediaSession)
    }

    companion object {
        private const val TAG = "MediaPlayerSession"
        fun getAvailableActions(@PlaybackStateCompat.State state: Int): Long {
            var actions = (PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_REWIND
                    or PlaybackStateCompat.ACTION_SEEK_TO)
            actions = if (state == PlaybackStateCompat.STATE_PLAYING) {
                actions or PlaybackStateCompat.ACTION_PAUSE
            } else {
                actions or PlaybackStateCompat.ACTION_PLAY
            }
            return actions
        }

        const val ACTION_SAVE_CURRENT_SONG_TO_FAVORITE = "action_save_to_favorite"
        fun switchPlayMode(
            transportControls: MediaControllerCompat.TransportControls,
            playMode: Int
        ) {
            if (playMode == PlayMode.ORDERLY) {
                transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
            } else if (playMode == PlayMode.SINGLE_LOOP) {
                transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
            } else {
                transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
            }
        }
    }
}