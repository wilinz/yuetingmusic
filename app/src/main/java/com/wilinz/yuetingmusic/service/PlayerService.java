package com.wilinz.yuetingmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.wilinz.yuetingmusic.data.model.Song;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PlayerService extends Service {

    private MediaPlayer mediaPlayer;

    private boolean isFirstPlay = true;
    private Timer timer;

    public PlayerService() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayEvent(PlayerEvent.PlayEvent event) {
        if (isFirstPlay) {
            try {
                mediaPlayer.setDataSource(event.audio.path);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener((player) -> {
                    isFirstPlay = false;
                    play();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            play();
        }
        // Do something
    }

    private void play() {
        mediaPlayer.start();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post(new PlayerEvent.ProgressChangeEvent(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration()));
            }
        }, 0, 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPauseEvent(PlayerEvent.PauseEvent event) {
        timer.cancel();
        mediaPlayer.pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSeekEvent(PlayerEvent.SeekEvent event) {
        mediaPlayer.seekTo((int) event.position);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new PlayerEvent.CreateMediaPlayerEvent(mediaPlayer));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        timer.cancel();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}