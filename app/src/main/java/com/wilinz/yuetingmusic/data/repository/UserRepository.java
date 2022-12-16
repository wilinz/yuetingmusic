package com.wilinz.yuetingmusic.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import androidx.core.net.UriKt;

import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.event.FavoriteUpdatedEvent;
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent;
import com.wilinz.yuetingmusic.event.UserChangeEvent;
import com.wilinz.yuetingmusic.util.MessageDigestUtil;
import com.wilinz.yuetingmusic.util.UriUtil;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.io.File;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRepository {

    public final static String visitorUserName = "visitor";

    public User getVisitorUser() {
        User user = new User();
        user.nickname = "访客登录";
        user.username = visitorUserName;
        user.password = "password";
        user.rememberPassword = true;
        return user;
    }

    public Observable<Optional<User>> loginOrSignupVisitorUser() {
        return getUser(visitorUserName)
                .map(user -> {
                    User visitorUser = getVisitorUser();
                    if (user.isPresent()) {
                        changeActive(user.get(), true, true);
                    } else {
                        signup(visitorUser).subscribe();
                    }
                    return Optional.ofNullable(visitorUser);
                });
    }

    public Observable<Optional<User>> getUser(String username) {
        return Observable.fromCallable(() -> {
            User user = LitePal.where("username=?", username).findFirst(User.class);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<User>> getAllUser() {
        return Observable.fromCallable(() -> LitePal.findAll(User.class)).subscribeOn(Schedulers.io());
    }

    public Observable<Optional<User>> getActiveUser() {
        return Observable.fromCallable(() -> {
            User user = LitePal.where("isactive = ?", 1 + "").findFirst(User.class);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.io());
    }

    public Observable<User> setUserAvatar(Context context, User user, Uri avatar) {
        return Observable.fromCallable(() -> {
            File dir = new File(context.getFilesDir(), "avatars");
            String rawFilename = UriUtil.getFileName(context, avatar);
            String ext = FilenameUtils.getExtension(rawFilename);
            Uri newUri = Uri.fromFile(UriUtil.copyToDir(context, dir, avatar, System.currentTimeMillis() + "." + ext));
            if (user.avatar != null) {
                File oldFile = UriKt.toFile(Uri.parse(user.avatar));
                oldFile.delete();
            }
            user.avatar = newUri.toString();
            user.save();
            return user;
        }).subscribeOn(Schedulers.io());
    }

    public Observable<User> changeActive(User user, boolean isActive, boolean... rememberPassword) {
        return Observable.fromCallable(() -> {
            LitePal.beginTransaction();

            if (isActive) {
                offlineOtherUsers();
            } else {
                user.setToDefault("isactive");
            }
            if (rememberPassword.length > 0) {
                if (rememberPassword[0]) user.rememberPassword = true;
                else user.setToDefault("rememberpassword");
            }
            user.isActive = isActive;
            user.updateAll("username = ?", user.username);
            LitePal.setTransactionSuccessful();
            LitePal.endTransaction();
            if (isActive) {
                EventBus.getDefault().post(new FavoriteUpdatedEvent());
                EventBus.getDefault().post(new RecentRecordUpdatedEvent());
                EventBus.getDefault().post(new UserChangeEvent());
            }
            return user;
        }).subscribeOn(Schedulers.io());
    }

    private void offlineOtherUsers() {
        ContentValues values = new ContentValues();
        values.put("isactive", 0);
        LitePal.updateAll(User.class, values, "isactive = ?", 1 + "");
    }

    public Observable<User> signup(String username, String password, boolean rememberPassword) {
        return Observable.fromCallable(() -> {
            User user1 = new User();
            user1.nickname = username;
            user1.username = username;
            user1.password = MessageDigestUtil.sumSha256(password);
            if (rememberPassword) user1.rememberPassword = true;
            else user1.setToDefault("rememberpassword");
            user1.isActive = true;

            LitePal.beginTransaction();
            offlineOtherUsers();
            if (user1.save()) LitePal.setTransactionSuccessful();
            LitePal.endTransaction();
            return user1;
        }).subscribeOn(Schedulers.io());
    }

    public Observable<User> signup(User user) {
        return Observable.fromCallable(() -> {
            user.password = MessageDigestUtil.sumSha256(user.password);
            user.isActive = true;

            LitePal.beginTransaction();
            offlineOtherUsers();
            if (user.save()) LitePal.setTransactionSuccessful();
            LitePal.endTransaction();
            EventBus.getDefault().post(new FavoriteUpdatedEvent());
            EventBus.getDefault().post(new RecentRecordUpdatedEvent());
            EventBus.getDefault().post(new UserChangeEvent());
            return user;
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