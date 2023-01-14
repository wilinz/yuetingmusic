package com.wilinz.yuetingmusic.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.data.repository.UserRepository
import com.wilinz.yuetingmusic.event.UserChangeEvent
import io.reactivex.rxjava3.core.Observable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class UserListViewModel : ViewModel() {
    private val usersLiveDate = MutableLiveData<List<User>>()
    fun getUsersLiveDate(): LiveData<List<User>> {
        return usersLiveDate
    }

    val allUser: Observable<List<User>>
        get() = UserRepository.instance!!.allUser
            .doOnNext { users: List<User> ->
                refreshingLiveData.postValue(false)
                usersLiveDate.postValue(users)
            }

    fun exitLogin(): Observable<Optional<User>> {
        return UserRepository.instance!!.loginOrSignupVisitorUser()
    }

    fun changeActive(
        user: User,
        isActive: Boolean,
        vararg rememberPassword: Boolean
    ): Observable<User> {
        return UserRepository.instance!!.changeActive(user, isActive, *rememberPassword)
    }

    protected var refreshingLiveData = MutableLiveData<Boolean>()

    init {
        EventBus.getDefault().register(this)
        allUser.subscribe()
    }

    fun getRefreshingLiveData(): LiveData<Boolean> {
        return refreshingLiveData
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshUser(event: UserChangeEvent?) {
        allUser.subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}