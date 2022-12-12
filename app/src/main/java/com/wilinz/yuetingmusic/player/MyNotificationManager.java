package com.wilinz.yuetingmusic.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableKt;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.wilinz.yuetingmusic.MainActivity;
import com.wilinz.yuetingmusic.R;

import java.util.Objects;

public class MyNotificationManager {

    private final static String TAG = "MyNotificationManager";
    public final String channelId = "playback_control";
    public final int notificationId = 56132;

    private final Context context;
    private final String channelName;
    private final MediaSessionCompat mediaSession;
    private final NotificationManager notificationManager;

    private boolean isShowNotification = true;

    public boolean isShowNotification() {
        return isShowNotification;
    }

    public void setShowNotification(boolean isShowNotification) {
        this.isShowNotification = isShowNotification;
    }

    public MyNotificationManager(Context context, MediaSessionCompat mediaSession) {
        this.context = context;
        this.channelName = context.getString(R.string.playback_control_notification);
        this.mediaSession = mediaSession;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private final MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    updateNotificationIfNeed();
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    updateNotificationIfNeed();
                }
            };

    private void updateNotificationIfNeed() {
        if (isShowNotification) {
            notificationManager.notify(notificationId, createPlayerNotification());
        }
    }

    public void registerCallback() {
        mediaSession.getController().registerCallback(controllerCallback);
    }

    public void unregisterCallback() {
        mediaSession.getController().unregisterCallback(controllerCallback);
    }

    public Notification createPlayerNotification() {
        createChannel(context, channelId, channelName, NotificationManagerCompat.IMPORTANCE_LOW);
        return createPlayerNotificationBuilder(context, channelId, mediaSession).build();
    }

    public static void createChannel(Context context, String channelId, String channelName, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public NotificationCompat.Builder createPlayerNotificationBuilder(Context context, String channelId, MediaSessionCompat mediaSession) {
        // Given a media session and its context (usually the component containing the session)
        // Create a NotificationCompat.Builder
        CharSequence title = "";
        CharSequence subtitle = "";
        CharSequence desc = "";
        Bitmap largeIcon = null;

        // Get the session's metadata
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();

        Uri iconUri = null;
        if (mediaMetadata != null) {
            MediaDescriptionCompat description = mediaMetadata.getDescription();
            if (description != null) {
                title = description.getTitle();
                subtitle = description.getSubtitle();
                desc = description.getDescription();
                largeIcon = description.getIconBitmap();
                iconUri = description.getIconUri();
            }
        }

        boolean isPlaying = controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

//        if (largeIcon == null) {
//            largeIcon = DrawableKt.toBitmap(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.noti)), 144, 144, null);
//        }
//        builder.setLargeIcon(largeIcon);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_MUTABLE);

        builder
                // 添加当前播放曲目的元数据
                .setContentTitle(title)
                .setContentText(subtitle)
                .setSubText(desc)

                // 通过单击通知启用启动播放器

                .setContentIntent(pendingIntent)

                // 滑动通知时停止服务
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))

                // 使传输控件在锁定屏幕上可见
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // 添加应用程序图标并设置其强调色
                // 注意颜色
                .setSmallIcon(R.drawable.logo)
                .setColor(ContextCompat.getColor(context, R.color.my_light_primary))

                .addAction(new NotificationCompat.Action(
                        R.drawable.skip_previous, context.getString(R.string.skip_previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))

                // 添加暂停按钮
                .addAction(new NotificationCompat.Action(
                        isPlaying ? R.drawable.round_pause_24 : R.drawable.round_play_arrow_24, context.getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))

                .addAction(new NotificationCompat.Action(
                        R.drawable.skip_next, context.getString(R.string.skip_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                // 利用 MediaStyle 功能
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)

                        // 添加取消按钮
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)));

        Log.d(TAG, "createPlayerNotificationBuilder: " + iconUri);
        Glide.with(context)
                .asBitmap()
                .load(iconUri)
                .error(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>(144, 144) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        MediaMetadataCompat mediaMetadataCompat1 = mediaSession.getController().getMetadata();
                        if (mediaMetadata != null && mediaMetadataCompat1 != null) {
                            MediaDescriptionCompat desc1 = mediaMetadata.getDescription();
                            MediaDescriptionCompat desc2 = mediaMetadataCompat1.getDescription();
                            if (desc1 != null && desc2 != null) {
                                if (!Objects.equals(desc1.getMediaId(), desc2.getMediaId())) {
                                    return;
                                }
                            }
                        }

                        builder.setLargeIcon(resource);
                        MyNotificationManager.this.notificationManager.notify(notificationId, builder.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
        return builder;

    }
}
