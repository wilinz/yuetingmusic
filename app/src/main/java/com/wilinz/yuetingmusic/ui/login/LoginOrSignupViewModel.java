package com.wilinz.yuetingmusic.ui.login;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.util.MessageDigestUtil;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginOrSignupViewModel extends AndroidViewModel {

    public MutableLiveData<Boolean> getSignupResult() {
        return signupResult;
    }

    private MutableLiveData<Boolean> signupResult = new MutableLiveData<>();


    public LoginOrSignupViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean login(User user, String password) {
        boolean isPasswordValid = user.password.equals(MessageDigestUtil.sumSha256(password));
        if (isPasswordValid) Pref.getInstance(getApplication()).setCurrentLoginEmail(user.email);
        return isPasswordValid;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void signup(String email, String password) {
        Observable.fromAction(() -> {
                    User user1 = new User();
                    user1.email = email;
                    user1.password = MessageDigestUtil.sumSha256(password);
                    user1.save();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {

                }, e -> {
                    signupResult.setValue(false);
                    Toast.makeText(getApplication(), "注册失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                }, () -> {
                    signupResult.setValue(true);
                    Toast.makeText(getApplication(), "注册成功", Toast.LENGTH_SHORT).show();
                    Pref.getInstance(getApplication()).setCurrentLoginEmail(email);
                });
    }

}
