package com.wilinz.yuetingmusic.ui.main.profile;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.UserRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class ProfileViewModel extends AndroidViewModel {

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<User> userLiveData = new MutableLiveData<>();


    public ProfileViewModel(@NonNull Application application) {
        super(application);
        getUser();
    }

    public void getUser() {
        UserRepository.getInstance().getUser(Pref.getInstance(getApplication()).getCurrentLoginEmail())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    user.ifPresent(value -> userLiveData.setValue(value));
                });
    }

    public Observable<User> setUserAvatar(User user, Uri avatar) {
        return UserRepository.getInstance().setUserAvatar(getApplication(), user, avatar)
                .doOnNext((user1)->{
                    userLiveData.postValue(user1);
                });
    }

}
