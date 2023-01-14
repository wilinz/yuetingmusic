package com.wilinz.yuetingmusic.ui.welcome

import android.annotation.SuppressLint
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.data.repository.UserRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.*

class WelcomeViewModel(application: Application) : AndroidViewModel(application) {
    fun getUser(email: String): Observable<Optional<User>> {
        return UserRepository.instance!!.getUser(email)
    }

    val signupResult = MutableLiveData<Boolean>()
    @SuppressLint("CheckResult")
    fun signupVisitor() {
        UserRepository.instance!!
            .loginOrSignupVisitorUser()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ o -> }, { e: Throwable ->
                signupResult.value = false
                e.printStackTrace()
                Toast.makeText(getApplication(), "游客登录失败：$e", Toast.LENGTH_SHORT).show()
            }) {
                signupResult.value = true
                Toast.makeText(getApplication(), "游客登录成功", Toast.LENGTH_SHORT).show()
                getInstance(getApplication())!!.isFirstLaunch = false
            }
    }
}