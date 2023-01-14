package com.wilinz.yuetingmusic.player

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import com.wilinz.yuetingmusic.util.LogUtil.d

class MyAudioManager(private val context: Context, mediaSession: MediaSessionCompat) {
    var playbackNowAuthorized = false
    private val focusLock = Any()
    private var playbackDelayed = false
    private var resumeOnFocusGain = false
    private val audioManager: AudioManager
    private val handler = Handler()
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    private val mediaSession: MediaSessionCompat
    fun requestAudioFocus(): Boolean {
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val focusRequest: AudioFocusRequest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            val res = audioManager.requestAudioFocus(focusRequest)
            synchronized(focusLock) {
                if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    playbackNowAuthorized = false
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    playbackNowAuthorized = true
                    return true
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                    playbackDelayed = true
                    playbackNowAuthorized = false
                }
            }
        } else {

            // Request audio focus for playback
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,  // Use the music stream.
                AudioManager.STREAM_MUSIC,  // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        return false
    }

    private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (playbackDelayed || resumeOnFocusGain) {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = false
                }
                play()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                d(TAG, "AudioManager.AUDIOFOCUS_LOSS")
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
                d(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT")
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {}
        }
    }

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        this.mediaSession = mediaSession
    }

    //    当音频输出切回到内置扬声器时，系统会广播 ACTION_AUDIO_BECOMING_NOISY Intent。
    internal inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                // Pause the playback
                d(Companion.TAG, "onReceive")
                pause()
            }
        }

        private val TAG = "BecomingNoisyReceiver"

    }

    fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }

    //MediaSessionCompat.Callback onPlay时调用
    fun registerBecomingNoisyReceiver() {
        context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
    }

    //MediaSessionCompat.Callback onStop时调用
    fun unregisterBecomingNoisyReceiver() {
        try {
            context.unregisterReceiver(myNoisyAudioStreamReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun pause() {
        mediaSession.controller.transportControls.pause()
    }

    protected fun play() {
        mediaSession.controller.transportControls.play()
    }

    protected fun stop() {
        mediaSession.controller.transportControls.stop()
    }

    companion object {
        private const val TAG = "MyAudioManager"

        //设置调整音量时调整的是媒体音量
        fun setVolumeControlStream(activity: Activity) {
            val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (!audioManager.isVolumeFixed) {
                activity.volumeControlStream = AudioManager.STREAM_MUSIC
            }
        }
    }
}