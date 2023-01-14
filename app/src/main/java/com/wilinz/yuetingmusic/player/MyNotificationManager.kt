package com.wilinz.yuetingmusic.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.wilinz.yuetingmusic.Key
import com.wilinz.yuetingmusic.MainActivity
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.constant.PlayMode

class MyNotificationManager(private val context: Context, mediaSession: MediaSessionCompat) {
    val channelId = "playback_control"
    val notificationId = 56132
    private val channelName: String
    private val mediaSession: MediaSessionCompat
    private val notificationManager: NotificationManager
    var isShowNotification = true
    private val controllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                updateNotificationIfNeed()
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                updateNotificationIfNeed()
            }
        }

    private fun updateNotificationIfNeed() {
        if (isShowNotification) {
            notificationManager.notify(notificationId, createPlayerNotification())
        }
    }

    fun registerCallback() {
        mediaSession.controller.registerCallback(controllerCallback)
    }

    fun unregisterCallback() {
        mediaSession.controller.unregisterCallback(controllerCallback)
    }

    fun createPlayerNotification(): Notification {
        createChannel(context, channelId, channelName, NotificationManagerCompat.IMPORTANCE_LOW)
        return createPlayerNotificationBuilder(context, channelId, mediaSession).build()
    }

    fun createPlayerNotificationBuilder(
        context: Context,
        channelId: String?,
        mediaSession: MediaSessionCompat
    ): NotificationCompat.Builder {
        // Given a media session and its context (usually the component containing the session)
        // Create a NotificationCompat.Builder
        var title: CharSequence = ""
        var subtitle: CharSequence = ""
        var desc: CharSequence = ""
        var largeIcon: Bitmap? = null

        // Get the session's metadata
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata
        var playMode = getInstance(context)!!.playMode
        val state = controller.playbackState
        val bundle = state.extras
        if (bundle != null) {
            val playMode1 = state.extras!!
                .getInt(Key.playMode, PlayMode.UNKNOWN)
            if (playMode1 != PlayMode.UNKNOWN) {
                playMode = playMode1
            }
        }
        var playModeResId = 0
        playModeResId = if (playMode == PlayMode.ORDERLY) {
            R.drawable.round_repeat_24
        } else if (playMode == PlayMode.SINGLE_LOOP) {
            R.drawable.round_repeat_one_24
        } else {
            R.drawable.round_shuffle_24
        }
        var iconUri: Uri? = null
        if (mediaMetadata != null) {
            val description = mediaMetadata.description
            if (description != null) {
                title = description.title?:""
                subtitle = description.subtitle?:""
                desc = description.description?:""
                largeIcon = description.iconBitmap
                iconUri = description.iconUri
            }
        }
        val isPlaying = controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING
        val builder = NotificationCompat.Builder(context, channelId!!)

//        if (largeIcon == null) {
//            largeIcon = DrawableKt.toBitmap(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.noti)), 144, 144, null);
//        }
//        builder.setLargeIcon(largeIcon);
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_MUTABLE
        )
        val favoritePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_SAVE_CURRENT_SONG_TO_FAVORITE),
            PendingIntent.FLAG_MUTABLE
        )
        val switchPlayModePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_SWITCH_PLAYBACK_MODE),
            PendingIntent.FLAG_MUTABLE
        )
        builder // 添加当前播放曲目的元数据
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText(desc) // 通过单击通知启用启动播放器
            .setContentIntent(pendingIntent) // 滑动通知时停止服务
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            ) // 使传输控件在锁定屏幕上可见
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 添加应用程序图标并设置其强调色
            // 注意颜色
            .setSmallIcon(R.drawable.logo)
            .setColor(ContextCompat.getColor(context, R.color.my_primary))
            .addAction(
                NotificationCompat.Action(
                    R.drawable.outline_favorite_border_24, context.getString(R.string.favorite),
                    favoritePendingIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_previous, context.getString(R.string.skip_previous),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            ) // 添加暂停按钮
            .addAction(
                NotificationCompat.Action(
                    if (isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                    context.getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_next, context.getString(R.string.skip_next),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    playModeResId, context.getString(R.string.switch_playback_modes),
                    switchPlayModePendingIntent
                )
            ) // 利用 MediaStyle 功能
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0) // 添加取消按钮
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        Log.d(TAG, "createPlayerNotificationBuilder: $iconUri")
        Glide.with(context)
            .asBitmap()
            .load(iconUri)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>(144, 144) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val mediaMetadataCompat1 = mediaSession.controller.metadata
                    if (mediaMetadata != null && mediaMetadataCompat1 != null) {
                        val desc1 = mediaMetadata.description
                        val desc2 = mediaMetadataCompat1.description
                        if (desc1 != null && desc2 != null) {
                            if (desc1.mediaId != desc2.mediaId) {
                                return
                            }
                        }
                    }
                    builder.setLargeIcon(resource)
                    notificationManager.notify(notificationId, builder.build())
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        return builder
    }

    private val playerBroadcastReceiver = PlayerBroadcastReceiver()
    private val intentFilter =
        IntentFilter(ACTION_SAVE_CURRENT_SONG_TO_FAVORITE)

    init {
        channelName = context.getString(R.string.playback_control_notification)
        this.mediaSession = mediaSession
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun registerPlayerBroadcastReceiver() {
        if (!intentFilter.hasAction(ACTION_SWITCH_PLAYBACK_MODE)) {
            intentFilter.addAction(ACTION_SWITCH_PLAYBACK_MODE)
        }
        context.registerReceiver(playerBroadcastReceiver, intentFilter)
    }

    //MediaSessionCompat.Callback onStop时调用
    fun unregisterPlayerBroadcastReceiver() {
        try {
            context.unregisterReceiver(playerBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal inner class PlayerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_SAVE_CURRENT_SONG_TO_FAVORITE) {
                mediaSession.controller.transportControls.sendCustomAction(
                    PlayerManager.ACTION_SAVE_CURRENT_SONG_TO_FAVORITE,
                    null
                )
            } else if (intent.action == ACTION_SWITCH_PLAYBACK_MODE) {
                PlayerManager.switchPlayMode(
                    mediaSession.controller.transportControls,
                    getInstance(context)!!.playMode
                )
            }
        }


    }

    companion object {
        private const val TAG = "MyNotificationManager"
        fun createChannel(
            context: Context,
            channelId: String?,
            channelName: String?,
            importance: Int
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(channelId, channelName, importance)
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val ACTION_SAVE_CURRENT_SONG_TO_FAVORITE =
            "com.wilinz.yuetingmusic.ACTION_SAVE_TO_FAVORITE"
        val ACTION_SWITCH_PLAYBACK_MODE =
            "com.wilinz.yuetingmusic.ACTION_SWITCH_PLAYBACK_MODE"
    }
}