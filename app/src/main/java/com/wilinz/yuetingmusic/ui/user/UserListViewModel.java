package com.wilinz.yuetingmusic.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.data.repository.UserRepository;
import com.wilinz.yuetingmusic.event.UserChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;

public class UserListViewModel extends ViewModel {

    public UserListViewModel() {
        EventBus.getDefault().register(this);
        getAllUser().subscribe();
    }

    private final MutableLiveData<List<User>> usersLiveDate = new MutableLiveData<>();

    public LiveData<List<User>> getUsersLiveDate() {
        return usersLiveDate;
    }

    public Observable<List<User>> getAllUser() {
        return UserRepository.getInstance().getAllUser()
                .doOnNext(users -> {
                    refreshingLiveData.postValue(false);
                    usersLiveDate.postValue(users);
                });
    }

    public Observable<Optional<User>> exitLogin() {
        return UserRepository.getInstance().loginOrSignupVisitorUser();
    }

    public Observable<User> changeActive(User user, boolean isActive) {
        return UserRepository.getInstance().changeActive(user, isActive);
    }

    protected MutableLiveData<Boolean> refreshingLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getRefreshingLiveData() {
        return refreshingLiveData;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUser(UserChangeEvent event){
        getAllUser().subscribe();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        EventBus.getDefault().unregister(this);
    }
}