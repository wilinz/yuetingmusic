package com.wilinz.yuetingmusic.data.repository;

import com.wilinz.yuetingmusic.data.model.User;

import org.litepal.LitePal;

import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRepository {

    public Observable<Optional<User>> getUser(String email) {
        return Observable.fromCallable(() -> {
            User user = LitePal.where("email=?", email).findFirst(User.class);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.io());
    }

    private static volatile UserRepository singleton;

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        if (singleton == null) {
            synchronized (UserRepository.class) {
                if (singleton == null) {
                    singleton = new UserRepository();
                }
            }
        }
        return singleton;
    }
}