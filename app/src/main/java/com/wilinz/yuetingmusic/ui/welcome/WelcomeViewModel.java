package com.wilinz.yuetingmusic.ui.welcome;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.UserRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;


public class WelcomeViewModel extends AndroidViewModel {

    public WelcomeViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    private MutableLiveData<User> userLiveData = new MutableLiveData<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void getUser(String email) {
        UserRepository.getInstance().getUser(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (user -> {
                            userLiveData.setValue(user.orElse(null));
                        }),
                        err -> {
                            err.printStackTrace();
                            Toast.makeText(getApplication(), "登录或注册失败：" + err.toString(), Toast.LENGTH_LONG).show();
                        });
    }

}
