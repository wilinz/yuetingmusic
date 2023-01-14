package com.wilinz.yuetingmusic.player

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.media.MediaBrowserServiceCompat
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.repository.SongRepository.Companion.instance
import com.wilinz.yuetingmusic.util.LogUtil.d

class MusicService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    private var stateBuilder: PlaybackStateCompat.Builder? = null
    private var myAudioManager: MyAudioManager? = null
    private var myNotificationManager: MyNotificationManager? = null
    private var exoPlayerManager: PlayerManager? = null
    override fun onCreate() {
        super.onCreate()
        d(TAG, "onCreate")
        createMediaSession()
    }

    private fun createMediaSession() {
        mediaSession = MediaSessionCompat(this, "MediaSessionCompat")
        // 启用来自 MediaButtons 和 TransportControls 的回调
        mediaSession!!.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        // 使用 ACTION_PLAY 设置初始 PlaybackState，以便媒体按钮可以启动播放器
        stateBuilder = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
            .setActions(PlayerManager.getAvailableActions(PlaybackStateCompat.STATE_NONE))
        mediaSession!!.setPlaybackState(stateBuilder!!.build())
        myAudioManager = MyAudioManager(this, mediaSession!!)
        myNotificationManager = MyNotificationManager(this, mediaSession!!)
        myNotificationManager!!.registerPlayerBroadcastReceiver()
        exoPlayerManager = PlayerManager(this, myAudioManager, myNotificationManager,
            mediaSession!!
        )
        // MySessionCallback() 具有处理来自媒体控制器的回调的方法
        mediaSession!!.setCallback(exoPlayerManager)

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession!!.sessionToken
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        //（可选）控制指定包名称的访问级别。您需要编写自己的逻辑来执行此操作。返回一个根 ID，客户端可以使用它与 onLoadChildren() 一起检索内容层次结构。
        exoPlayerManager!!.sendMetadata()
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        // 不允许浏览
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null)
            return
        }
        // 例如假设音乐目录已经加载缓存。
        result.sendResult(instance!!.playQueue.map { obj: Song -> obj.mapToMediaItem() })
    }

    override fun onDestroy() {
        super.onDestroy()
        myNotificationManager!!.unregisterPlayerBroadcastReceiver()
        exoPlayerManager!!.unregister()
    }

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val TAG = "MusicService"
    }
}