package com.wilinz.yuetingmusic.ui.main.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.data.repository.SongRepository
import com.wilinz.yuetingmusic.data.repository.UserRepository
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent
import com.wilinz.yuetingmusic.event.UserChangeEvent
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ProfileViewModel(application: Application) : MediaControllerViewModel(application) {
    var userLiveData = MutableLiveData<User>()
    fun getRecentListLiveData(): LiveData<List<Song>> {
        return recentListLiveData
    }

    private val recentListLiveData = MutableLiveData<List<Song>>()
    val recentList: Observable<List<Song>>
        get() = SongRepository.instance!!.getRecentList(5)
            .doOnNext { songs: List<Song> -> recentListLiveData.postValue(songs) }

    init {
        EventBus.getDefault().register(this)
        user.subscribe()
        recentList.subscribe()
    }

    val user: Observable<Optional<User>>
        get() = UserRepository.instance!!.activeUser
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { user: Optional<User> ->
                Log.d(TAG, "user: $user")
                user.ifPresent { value: User -> userLiveData.setValue(value) }
            }

    fun setUserAvatar(user: User, avatar: Uri): Observable<User> {
        return UserRepository.instance!!.setUserAvatar(getApplication(), user, avatar)
            .doOnNext { user1: User -> userLiveData.postValue(user1) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshSong(event: RecentRecordUpdatedEvent?) {
        recentList.subscribe()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshUser(event: UserChangeEvent?) {
        user.subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}