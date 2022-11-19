package com.wilinz.yuetingmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.wilinz.yuetingmusic.MainActivity;
import com.wilinz.yuetingmusic.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service {

    private static boolean isStarted = false;

    public static void start(@NonNull Context context) {
        if (isStarted) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, PlayerService.class));
        } else {
            context.startService(new Intent(context, PlayerService.class));
        }
    }

    private MediaPlayer mediaPlayer;

    NotificationManager notificationManager;
    private boolean isFirstPlay = true;
    private Timer timer;

    public PlayerService() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayEvent(PlayerEvent.PlayEvent event) {
        notificationManager.notify(notificationId, createNotification(true));
        if (isFirstPlay) {
            try {
//                mediaPlayer.setDataSource(event.audio.uri);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener((player) -> {
                    isFirstPlay = false;
                    play();
                });
                mediaPlayer.setOnCompletionListener((player) -> {
                    mediaPlayer.reset();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            play();
        }
        // Do something
    }

    private MediaSessionManager mediaSessionManager;

    //Must be one of: NotificationManager.IMPORTANCE_UNSPECIFIED, NotificationManager.IMPORTANCE_NONE, NotificationManager.IMPORTANCE_MIN, NotificationManager.IMPORTANCE_LOW, NotificationManager.IMPORTANCE_DEFAULT, NotificationManager.IMPORTANCE_HIGH
    private Notification createNotification(boolean isPlaying) {
        createChannel();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = null;
        pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionManager.getMediaSession())
                .setShowActionsInCompactView(1, 0, 2, 3, 4);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "player_controller")
                .setSmallIcon(R.drawable.logo)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pi)
                .setStyle(style)
                .setContentTitle("悦听")
                .setContentText("起风了")
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.default_cover_place))
//                .addAction(R.drawable.outline_favorite_border_24,
//                        "收藏",
//                        pi)


//                .addAction(R.drawable.ic_lyric,
//                        "歌词",
//                        retrievePlaybackAction(ACTION_LYRIC))
//                .addAction(R.drawable.ic_clear,
//                        "关闭",
//                        retrievePlaybackAction(ACTION_CLOSE))
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this, PlaybackStateCompat.ACTION_STOP));
        notification.addAction(new NotificationCompat.Action(R.drawable.skip_previous,
                "上一首",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        if (!isPlaying) {
            notification.addAction(new NotificationCompat.Action(R.drawable.play_arrow_24px,
                    "播放",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                            PlaybackStateCompat.ACTION_PLAY)));
        } else {
            notification.addAction(new NotificationCompat.Action(R.drawable.round_pause_24,
                    "暂停",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                            PlaybackStateCompat.ACTION_PAUSE)));
        }
        notification.addAction(new NotificationCompat.Action(R.drawable.skip_next,
                "下一首",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.default_cover_place)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        notification.setLargeIcon(resource);
                        notificationManager.notify(notificationId, notification.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        return notification.build();

    }

    private static int notificationId = 1233;

    private void createChannel() {
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("player_controller", "音乐播放控制", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
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
        notificationManager.notify(notificationId, createNotification(false));
        timer.cancel();
        mediaPlayer.pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(PlayerEvent.StopEvent event) {
        notificationManager.notify(notificationId, createNotification(false));
        mediaPlayer.stop();
        mediaPlayer = null;
        stopSelf();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSeekEvent(PlayerEvent.SeekEvent event) {
        mediaPlayer.seekTo((int) event.position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPlayStatusEvent(PlayerEvent.GetPlayStatusEvent event) {
        Log.d("GetPlayStatusEvent: ", "");
        EventBus.getDefault().post(new PlayerEvent.ProgressChangeEvent(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration()));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mediaSessionManager = new MediaSessionManager(this);
        startForeground(notificationId, createNotification(false));
        mediaPlayer = new MediaPlayer();
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new PlayerEvent.CreateMediaPlayerEvent(mediaPlayer));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSessionManager.mMediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
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