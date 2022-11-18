package com.wilinz.yuetingmusic.service;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class MyAudioManagerImpl extends MyAudioManager {

    private final MediaSessionCompat mediaSession;

    public MyAudioManagerImpl(Context context, MediaSessionCompat mediaSession) {
        super(context);
        this.mediaSession = mediaSession;
    }

    @Override
    protected void pause() {
        mediaSession.getController().getTransportControls().pause();
    }

    @Override
    protected void play() {
        mediaSession.getController().getTransportControls().play();
    }

    @Override
    protected void stop() {
        mediaSession.getController().getTransportControls().stop();
    }
}
