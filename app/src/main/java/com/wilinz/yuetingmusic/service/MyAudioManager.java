package com.wilinz.yuetingmusic.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;

import java.util.concurrent.TimeUnit;

public abstract class MyAudioManager {

    public boolean playbackNowAuthorized = false;
    private final Context context;
    private final Object focusLock = new Object();
    private boolean playbackDelayed = false;
    private boolean resumeOnFocusGain = false;
    private final AudioManager audioManager;
    private final Handler handler = new Handler();
    private final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();

    public MyAudioManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean requestAudioFocus() {

        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFocusRequest focusRequest;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this::onAudioFocusChange)
                    .build();

            int res = audioManager.requestAudioFocus(focusRequest);
            synchronized (focusLock) {
                if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    playbackNowAuthorized = false;
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    playbackNowAuthorized = true;
                    return true;
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                    playbackDelayed = true;
                    playbackNowAuthorized = false;
                }
            }
        } else {

            // Request audio focus for playback
            int result = audioManager.requestAudioFocus(
                    this::onAudioFocusChange,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        }

        return false;
    }

    private void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized (focusLock) {
                        playbackDelayed = false;
                        resumeOnFocusGain = false;
                    }
                    play();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                synchronized (focusLock) {
                    resumeOnFocusGain = false;
                    playbackDelayed = false;
                }
                pause();
                // 停止播放前等待 30 秒
//                handler.postDelayed(this::stop, TimeUnit.SECONDS.toMillis(30));
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                synchronized (focusLock) {
                    resumeOnFocusGain = true;
                    playbackDelayed = false;
                }
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // ... pausing or ducking depends on your app
                break;
        }
    }

    //    当音频输出切回到内置扬声器时，系统会广播 ACTION_AUDIO_BECOMING_NOISY Intent。
    class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                pause();
            }
        }
    }

    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this::onAudioFocusChange);
    }

    //MediaSessionCompat.Callback onPlay时调用
    public void registerBecomingNoisyReceiver() {
        context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
    }

    //MediaSessionCompat.Callback onStop时调用
    public void unregisterBecomingNoisyReceiver() {
        try {
            context.unregisterReceiver(myNoisyAudioStreamReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置调整音量时调整的是媒体音量
    public static void setVolumeControlStream(Activity activity) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isVolumeFixed()) {
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }


    protected abstract void pause();

    protected abstract void play();

    protected abstract void stop();
}
