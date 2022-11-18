package com.wilinz.yuetingmusic.service;

import android.app.Service;
import android.support.v4.media.session.MediaSessionCompat;


abstract class MyMediaSessionCallback extends MediaSessionCompat.Callback {

    protected final Service service;
    protected final MyAudioManager myAudioManager;
    private final MyNotificationManager myNotificationManager;
    private boolean isStarted = false;

    public MyMediaSessionCallback(Service service, MyAudioManager myAudioManager, MyNotificationManager myNotificationManager) {
        this.service = service;
        this.myAudioManager = myAudioManager;
        this.myNotificationManager = myNotificationManager;
    }

    @Override
    public void onPlay() {
        super.onPlay();
        register();
        if (!isStarted) {
            service.startForeground(myNotificationManager.notificationId, myNotificationManager.createPlayerNotification());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unregister();
    }

    public void unregister(){
        if (isStarted) {
            isStarted = false;
            myAudioManager.unregisterBecomingNoisyReceiver();
            myAudioManager.abandonAudioFocus();
            myNotificationManager.unregisterCallback();
        }
    }

    public void register(){
        if (!isStarted) {
            isStarted = true;
            myAudioManager.registerBecomingNoisyReceiver();
//            myAudioManager.requestAudioFocus();
            myNotificationManager.registerCallback();
        }
    }
}