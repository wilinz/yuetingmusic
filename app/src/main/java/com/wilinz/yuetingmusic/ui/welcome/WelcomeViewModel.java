package com.wilinz.yuetingmusic.ui.welcome;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.UserRepository;

import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;


public class WelcomeViewModel extends AndroidViewModel {

    public WelcomeViewModel(@NonNull Application application) {
        super(application);
    }

    public Observable<Optional<User>> getUser(String email) {
        return UserRepository.getInstance().getUser(email);
    }

}
