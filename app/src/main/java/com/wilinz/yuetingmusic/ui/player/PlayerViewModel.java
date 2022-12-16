package com.wilinz.yuetingmusic.ui.player;

import android.app.Application;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.player.PlayerManager;
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel;

public class PlayerViewModel extends MediaControllerViewModel {
    public PlayerViewModel(@NonNull Application application) {
        super(application);
    }

    public void saveFavoriteSong() {
        getMediaController().getTransportControls().sendCustomAction(PlayerManager.ACTION_SAVE_CURRENT_SONG_TO_FAVORITE,null);
    }

}
