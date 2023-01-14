package com.wilinz.yuetingmusic.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.event.FavoriteUpdatedEvent
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent
import com.wilinz.yuetingmusic.event.UserChangeEvent
import com.wilinz.yuetingmusic.util.MessageDigestUtil.sumSha256
import com.wilinz.yuetingmusic.util.UriUtil.copyToDir
import com.wilinz.yuetingmusic.util.UriUtil.getFileName
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.io.FilenameUtils
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal.beginTransaction
import org.litepal.LitePal.endTransaction
import org.litepal.LitePal.findAll
import org.litepal.LitePal.setTransactionSuccessful
import org.litepal.LitePal.updateAll
import org.litepal.LitePal.where
import java.io.File
import java.util.*
import java.util.concurrent.Callable

class UserRepository private constructor() {
    val visitorUser: User
        get() {
            val user = User()
            user.nickname = "访客登录"
            user.username = visitorUserName
            user.password = "password"
            user.rememberPassword = true
            return user
        }

    fun loginOrSignupVisitorUser(): Observable<Optional<User>> {
        return getUser(visitorUserName)
            .map { user: Optional<User> ->
                val visitorUser = visitorUser
                if (user.isPresent) {
                    changeActive(user.get(), true, true)
                } else {
                    signup(visitorUser).subscribe()
                }
                Optional.ofNullable(visitorUser)
            }
    }

    fun getUser(username: String?): Observable<Optional<User>> {
        return Observable.fromCallable {
            val user = where("username=?", username).findFirst(
                User::class.java
            )
            Optional.ofNullable(user)
        }.subscribeOn(Schedulers.io())
    }

    val allUser: Observable<List<User>>
        get() = Observable.fromCallable {
            findAll(
                User::class.java
            )
        }.subscribeOn(Schedulers.io())
    val activeUser: Observable<Optional<User>>
        get() = Observable.fromCallable {
            val user = where("isactive = ?", 1.toString() + "").findFirst(
                User::class.java
            )
            Optional.ofNullable(user)
        }.subscribeOn(Schedulers.io())

    fun setUserAvatar(context: Context, user: User, avatar: Uri?): Observable<User> {
        return Observable.fromCallable {
            val dir = File(context.filesDir, "avatars")
            val rawFilename = getFileName(context, avatar!!)
            val ext = FilenameUtils.getExtension(rawFilename)
            val newUri = Uri.fromFile(
                copyToDir(
                    context,
                    dir,
                    avatar,
                    System.currentTimeMillis().toString() + "." + ext
                )
            )
            if (user.avatar != null) {
                val oldFile = Uri.parse(user.avatar).toFile()
                oldFile.delete()
            }
            user.avatar = newUri.toString()
            user.save()
            user
        }.subscribeOn(Schedulers.io())
    }

    fun changeActive(
        user: User,
        isActive: Boolean,
        vararg rememberPassword: Boolean
    ): Observable<User> {
        return Observable.fromCallable {
            beginTransaction()
            if (isActive) {
                offlineOtherUsers()
            } else {
                user.setToDefault("isactive")
            }
            if (rememberPassword.size > 0) {
                if (rememberPassword[0]) user.rememberPassword =
                    true else user.setToDefault("rememberpassword")
            }
            user.isActive = isActive
            user.updateAll("username = ?", user.username)
            setTransactionSuccessful()
            endTransaction()
            if (isActive) {
                EventBus.getDefault().post(FavoriteUpdatedEvent())
                EventBus.getDefault().post(RecentRecordUpdatedEvent())
                EventBus.getDefault().post(UserChangeEvent())
            }
            user
        }.subscribeOn(Schedulers.io())
    }

    private fun offlineOtherUsers() {
        val values = ContentValues()
        values.put("isactive", 0)
        updateAll(User::class.java, values, "isactive = ?", 1.toString() + "")
    }

    fun signup(username: String?, password: String?, rememberPassword: Boolean): Observable<User> {
        return Observable.fromCallable {
            val user1 = User()
            user1.nickname = username
            user1.username = username
            user1.password = sumSha256(password!!)
            if (rememberPassword) user1.rememberPassword =
                true else user1.setToDefault("rememberpassword")
            user1.isActive = true
            beginTransaction()
            offlineOtherUsers()
            if (user1.save()) setTransactionSuccessful()
            endTransaction()
            user1
        }.subscribeOn(Schedulers.io())
    }

    fun signup(user: User): Observable<User> {
        return Observable.fromCallable {
            user.password = sumSha256(user.password!!)
            user.isActive = true
            beginTransaction()
            offlineOtherUsers()
            if (user.save()) setTransactionSuccessful()
            endTransaction()
            EventBus.getDefault().post(FavoriteUpdatedEvent())
            EventBus.getDefault().post(RecentRecordUpdatedEvent())
            EventBus.getDefault().post(UserChangeEvent())
            user
        }.subscribeOn(Schedulers.io())
    }

    companion object {
        const val visitorUserName = "visitor"

        @Volatile
        private var singleton: UserRepository? = null
        val instance: UserRepository?
            get() {
                if (singleton == null) {
                    synchronized(UserRepository::class.java) {
                        if (singleton == null) {
                            singleton = UserRepository()
                        }
                    }
                }
                return singleton
            }
    }
}