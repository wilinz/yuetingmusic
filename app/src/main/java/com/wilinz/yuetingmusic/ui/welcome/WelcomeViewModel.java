package com.wilinz.yuetingmusic.ui.welcome;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.UserRepository;

import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;


public class WelcomeViewModel extends AndroidViewModel {

    public WelcomeViewModel(@NonNull Application application) {
        super(application);
    }

    public Observable<Optional<User>> getUser(String email) {
        return UserRepository.getInstance().getUser(email);
    }

    public MutableLiveData<Boolean> getSignupResult() {
        return signupResult;
    }

    private MutableLiveData<Boolean> signupResult = new MutableLiveData<>();


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void signupVisitor() {
        UserRepository.getInstance()
                .signup(UserRepository.getInstance().getVisitorUser())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                }, e -> {
                    signupResult.setValue(false);
                    e.printStackTrace();
                    Toast.makeText(getApplication(), "游客登录失败：" + e, Toast.LENGTH_SHORT).show();
                }, () -> {
                    signupResult.setValue(true);
                    Toast.makeText(getApplication(), "游客登录成功", Toast.LENGTH_SHORT).show();
                    Pref.getInstance(getApplication()).setFirstLaunch(false);
                });
    }

}
