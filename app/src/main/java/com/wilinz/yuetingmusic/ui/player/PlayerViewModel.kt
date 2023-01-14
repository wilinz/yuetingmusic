package com.wilinz.yuetingmusic.ui.player

import android.app.Application
import com.wilinz.yuetingmusic.player.PlayerManager
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel

class PlayerViewModel(application: Application) : MediaControllerViewModel(application) {
    fun saveFavoriteSong() {
        mediaController!!.transportControls.sendCustomAction(
            PlayerManager.ACTION_SAVE_CURRENT_SONG_TO_FAVORITE,
            null
        )
    }
}