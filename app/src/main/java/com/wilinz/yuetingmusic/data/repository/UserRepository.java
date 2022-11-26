package com.wilinz.yuetingmusic.data.repository;

import android.content.Context;
import android.net.Uri;

import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.util.UriUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.io.ByteStreamsKt;
import kotlin.io.FilesKt;

public class UserRepository {

    public Observable<Optional<User>> getUser(String email) {
        return Observable.fromCallable(() -> {
            User user = LitePal.where("email=?", email).findFirst(User.class);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.io());
    }

    public Observable<User> setUserAvatar(Context context, User user, Uri avatar) {
        return Observable.fromCallable(() -> {
            File dir = new File(context.getFilesDir(), "avatars");
            String rawFilename = UriUtil.getFileName(context, avatar);
            String ext = FilenameUtils.getExtension(rawFilename);
            Uri newUri = Uri.fromFile(UriUtil.copyToDir(context, dir, avatar, System.currentTimeMillis() + "." + ext));
//            if (user.avatar!=null)context.getContentResolver().delete(Uri.parse(user.avatar));
            user.avatar = newUri.toString();
            user.save();
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