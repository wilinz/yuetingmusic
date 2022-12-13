package com.wilinz.yuetingmusic.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.net.UriKt;

import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.util.MessageDigestUtil;
import com.wilinz.yuetingmusic.util.UriUtil;

import org.apache.commons.io.FilenameUtils;
import org.litepal.LitePal;

import java.io.File;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRepository {

    public Observable<Optional<User>> getUser(String username) {
        return Observable.fromCallable(() -> {
            User user = LitePal.where("username=?", username).findFirst(User.class);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.io());
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

    public Observable<User> changeActive(User user, boolean isActive) {
        return Observable.fromCallable(() -> {
            LitePal.beginTransaction();

            if (isActive) {
                offlineOtherUsers();
            } else {
                user.setToDefault("isactive");
            }

            user.isActive = isActive;
            user.updateAll("username = ?", user.username);
            LitePal.setTransactionSuccessful();
            LitePal.endTransaction();
            return user;
        }).subscribeOn(Schedulers.io());
    }

    private void offlineOtherUsers() {
        ContentValues values = new ContentValues();
        values.put("isactive", 0);
        LitePal.updateAll(User.class, values, "isactive = ?", 1 + "");
    }

    public Observable<User> signup(String username, String password) {
        return Observable.fromCallable(() -> {
            User user1 = new User();
            user1.username = username;
            user1.password = MessageDigestUtil.sumSha256(password);
            user1.isActive = true;

            LitePal.beginTransaction();
            offlineOtherUsers();
            if (user1.save()) LitePal.setTransactionSuccessful();
            LitePal.endTransaction();
            return user1;
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