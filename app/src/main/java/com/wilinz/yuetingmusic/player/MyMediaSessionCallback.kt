package com.wilinz.yuetingmusic.player

import android.app.Service
import android.support.v4.media.session.MediaSessionCompat

abstract class MyMediaSessionCallback(
    protected val service: Service,
    protected val myAudioManager: MyAudioManager,
    private val myNotificationManager: MyNotificationManager
) : MediaSessionCompat.Callback() {
    private var isStarted = false
    override fun onPlay() {
        super.onPlay()
        if (!isStarted) {
            service.startForeground(
                myNotificationManager.notificationId,
                myNotificationManager.createPlayerNotification()
            )
        }
    }

    override fun onStop() {
        super.onStop()
        unregister()
    }

    open fun unregister() {
        isStarted = false
        myAudioManager.unregisterBecomingNoisyReceiver()
        myAudioManager.abandonAudioFocus()
        myNotificationManager.unregisterCallback()
    }

    fun register() {
        isStarted = true
        myAudioManager.registerBecomingNoisyReceiver()
        myNotificationManager.registerCallback()
    }
}