package com.wilinz.yuetingmusic.ui.signinsignup

import android.annotation.SuppressLint
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.data.repository.UserRepository
import com.wilinz.yuetingmusic.util.MessageDigestUtil.sumSha256
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class LoginOrSignupViewModel(application: Application) : AndroidViewModel(application) {
    val signupResult = MutableLiveData<Boolean>()
    fun login(user: User, password: String?, rememberPassword: Boolean): Boolean {
        val isPasswordValid = user.password == sumSha256(password!!)
        if (isPasswordValid) {
            UserRepository.instance!!.changeActive(user, true, rememberPassword).subscribe()
        }
        return isPasswordValid
    }

    @SuppressLint("CheckResult")
    fun signup(username: String?, password: String?, rememberPassword: Boolean) {
        UserRepository.instance!!.signup(username, password, rememberPassword)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ o: User? -> }, { e: Throwable ->
                signupResult.value = false
                e.printStackTrace()
                Toast.makeText(getApplication(), "注册失败：$e", Toast.LENGTH_SHORT).show()
            }) {
                signupResult.value = true
                Toast.makeText(getApplication(), "注册成功", Toast.LENGTH_SHORT).show()
                getInstance(getApplication())!!.isFirstLaunch = false
            }
    }
}