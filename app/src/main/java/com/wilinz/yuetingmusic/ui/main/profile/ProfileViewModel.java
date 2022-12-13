package com.wilinz.yuetingmusic.ui.main.profile;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.UserRepository;

import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class ProfileViewModel extends AndroidViewModel {

    private final static String TAG = "ProfileViewModel";

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<User> userLiveData = new MutableLiveData<>();


    public ProfileViewModel(@NonNull Application application) {
        super(application);
        getUser();
    }

    public void getUser() {
        UserRepository.getInstance().getActiveUser()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
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

}
