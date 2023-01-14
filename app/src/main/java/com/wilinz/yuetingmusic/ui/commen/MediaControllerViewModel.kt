package com.wilinz.yuetingmusic.ui.commen

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wilinz.yuetingmusic.Key
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.constant.PlayMode
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.player.MusicService
import com.wilinz.yuetingmusic.player.PlayerManager
import com.wilinz.yuetingmusic.util.RxTimer
import java.util.*
import kotlin.math.abs

@SuppressLint("LongLogTag")
open class MediaControllerViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaMetadataLiveData = MutableLiveData<MediaMetadataCompat>()
    private val playStateLiveData = MutableLiveData<PlaybackStateCompat>()
    private val playPositionLiveData = MutableLiveData<Long>()
    private val playModeLiveData = MutableLiveData<Int>()
    fun getPlayModeLiveData(): LiveData<Int> {
        return playModeLiveData
    }

    fun getMediaMetadataLiveData(): LiveData<MediaMetadataCompat> {
        return mediaMetadataLiveData
    }

    fun getPlayPositionLiveData(): LiveData<Long> {
        return playPositionLiveData
    }

    val playbackStateLiveData: LiveData<PlaybackStateCompat>
        get() = playStateLiveData
    val playbackState: PlaybackStateCompat
        get() = mediaController!!.playbackState
    private lateinit var mediaBrowser: MediaBrowserCompat
    var mediaController: MediaControllerCompat? = null
        private set
    private var controllerCallback: MediaControllerCompat.Callback? =   object : MediaControllerCompat.Callback() {
        @SuppressLint("SetTextI18n")
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            mediaMetadataLiveData.value = metadata
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            playStateLiveData.value = state
            updatePlaybackState(state)
        }
    }
    private var timer: RxTimer? = null
    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                val token = mediaBrowser.sessionToken
                mediaController = MediaControllerCompat(
                    getApplication(),  // Context
                    token
                )
                buildTransportControls()
            }

            override fun onConnectionSuspended() {
                // 服务已崩溃。禁用传输控制，直到它自动重新连接
            }

            override fun onConnectionFailed() {
                // 该服务已拒绝我们的连接
            }
        }

    fun playFromUri(songs: List<Song?>?, song: Song) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(Key.songList, songs as ArrayList<out Parcelable?>?)
        mediaController!!.transportControls.playFromUri(Uri.parse(song.url), bundle)
    }

    private fun buildTransportControls() {
        mediaMetadataLiveData.value = mediaController!!.metadata
        playStateLiveData.value = mediaController!!.playbackState
        updatePlaybackState(mediaController!!.playbackState)
        playPositionLiveData.value = mediaController!!.playbackState.position
        // 注册回调以保持同步
        controllerCallback = object : MediaControllerCompat.Callback() {
            @SuppressLint("SetTextI18n")
            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                mediaMetadataLiveData.setValue(metadata)
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                playStateLiveData.setValue(state)
                updatePlaybackState(state)
            }
        }
        mediaController!!.registerCallback(controllerCallback!!)
    }

    override fun onCleared() {
        super.onCleared()
        if (timer != null) timer!!.cancel()
        //（请参阅“与 MediaSession 保持同步”）
        if (mediaController != null) {
            mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
        }
        mediaBrowser.disconnect()
    }

    fun pause() {
        mediaController!!.transportControls.pause()
    }

    fun play() {
        mediaController!!.transportControls.play()
    }

    fun seekTo(pos: Long) {
        mediaController!!.transportControls.seekTo(pos)
    }

    fun skipToPrevious() {
        mediaController!!.transportControls.skipToPrevious()
    }

    fun skipToNext() {
        mediaController!!.transportControls.skipToNext()
    }

    fun switchPlayMode() {
        val playModeInteger = getPlayModeLiveData().value
        val playMode = playModeInteger ?: PlayMode.ORDERLY
        PlayerManager.switchPlayMode(mediaController!!.transportControls, playMode)
    }

    private fun updatePlaybackState(state: PlaybackStateCompat) {
        playStateLiveData.value = state
        val bundle = state.extras
        if (bundle != null) {
            val playMode = state.extras!!
                .getInt(Key.playMode, PlayMode.UNKNOWN)
            if (playMode != PlayMode.UNKNOWN) {
                playModeLiveData.setValue(playMode)
            }
        }
        if (state.state == PlaybackStateCompat.STATE_NONE || state.state == PlaybackStateCompat.STATE_PAUSED) {
            if (timer != null) timer!!.cancel()
        } else {
            timer = RxTimer()
            timer!!.interval(1000) { number: Long ->
                val position = mediaController!!.playbackState.position
                playPositionLiveData.setValue(position)
            }
        }
    }

    private val updatePictureRotationLiveData = MutableLiveData<Float>()
    var pictureRotationTimer: RxTimer? = null

    init {
        playModeLiveData.value = getInstance(application)!!.playMode
        mediaBrowser = MediaBrowserCompat(
            getApplication(),
            ComponentName(getApplication(), MusicService::class.java),
            connectionCallbacks,
            null
        ) // optional Bundle
        mediaBrowser.connect()
    }

    fun getUpdatePictureRotationLiveData(): LiveData<Float> {
        return updatePictureRotationLiveData
    }

    fun startPictureRotationTimer(growth: Float) {
        if (pictureRotationTimer == null || pictureRotationTimer!!.isCanceled) {
//            stopPictureRotationTimer();
            pictureRotationTimer = RxTimer()
            pictureRotationTimer!!.interval(16) {
                var rotation = Optional.ofNullable(updatePictureRotationLiveData.value).orElse(0f)
                rotation=if (abs(rotation - (360 - growth)) <= 0.1) 0f else rotation + growth
                updatePictureRotationLiveData.setValue(rotation)
            }
        }
    }

    fun stopPictureRotationTimer() {
        if (pictureRotationTimer != null) pictureRotationTimer!!.cancel()
    }

    companion object {
        private const val TAG = "MediaControllerViewModel"
    }
}