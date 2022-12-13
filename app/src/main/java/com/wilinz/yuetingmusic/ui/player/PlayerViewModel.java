package com.wilinz.yuetingmusic.ui.player;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel;
import com.wilinz.yuetingmusic.util.RxTimer;

public class PlayerViewModel extends MediaControllerViewModel {
    public PlayerViewModel(@NonNull Application application) {
        super(application);
    }
}
