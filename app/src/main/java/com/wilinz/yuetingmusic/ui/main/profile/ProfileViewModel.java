package com.wilinz.yuetingmusic.ui.main.profile;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.SongRepository;
import com.wilinz.yuetingmusic.data.repository.UserRepository;
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent;
import com.wilinz.yuetingmusic.event.UserChangeEvent;
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class ProfileViewModel extends MediaControllerViewModel {

    private final static String TAG = "ProfileViewModel";

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public LiveData<List<Song>> getRecentListLiveData() {
        return recentListLiveData;
    }

    private MutableLiveData<List<Song>> recentListLiveData = new MutableLiveData<>();

    public Observable<List<Song>> getRecentList() {
        return SongRepository.getInstance().getRecentList(5)
                .doOnNext(songs -> {
                    recentListLiveData.postValue(songs);
                });
    }

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        EventBus.getDefault().register(this);
        getUser().subscribe();
        getRecentList().subscribe();
    }

    public Observable<Optional<User>>  getUser() {
        return UserRepository.getInstance().getActiveUser()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(user -> {
                    Log.d(TAG, "user: " + user);
                    user.ifPresent(value -> userLiveData.setValue(value));
                });
    }

    public Observable<User> setUserAvatar(User user, Uri avatar) {
        return UserRepository.getInstance().setUserAvatar(getApplication(), user, avatar)
                .doOnNext((user1) -> {
                    userLiveData.postValue(user1);
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshSong(RecentRecordUpdatedEvent event){
        getRecentList().subscribe();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUser(UserChangeEvent event){
        getUser().subscribe();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        EventBus.getDefault().unregister(this);
    }
}
