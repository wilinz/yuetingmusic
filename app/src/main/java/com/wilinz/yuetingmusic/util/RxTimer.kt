package com.wilinz.yuetingmusic.util

import androidx.lifecycle.LifecycleOwner
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class RxTimer {
    private var mDisposable: Disposable? = null
    fun timer(milliSeconds: Long, rxAction: ((number: Long)->Unit)?) {
        timer(null, milliSeconds, rxAction)
    }

    /**
     * milliseconds毫秒后执行指定动作
     *
     * @param milliSeconds
     * @param rxAction
     */
    fun timer(owner: LifecycleOwner?, milliSeconds: Long, rxAction: ((number: Long)->Unit)?) {
        var observable = Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
        if (owner != null) {
            observable = observable.compose(
                AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle()
            )
        }
        observable.subscribe(object : Observer<Long> {
            override fun onSubscribe(disposable: Disposable) {
                mDisposable = disposable
            }

            override fun onNext(number: Long) {
                rxAction?.invoke(number)
            }

            override fun onError(e: Throwable) {
                //取消订阅
                cancel()
            }

            override fun onComplete() {
                //取消订阅
                cancel()
            }
        })
    }

    fun interval(milliSeconds: Long, rxAction: ((number: Long)->Unit)?) {
        interval(null, milliSeconds, rxAction)
    }

    /**
     * 每隔milliseconds毫秒后执行指定动作
     *
     * @param milliSeconds
     * @param rxAction
     */
    fun interval(owner: LifecycleOwner?, milliSeconds: Long, rxAction: ((number: Long)->Unit)?) {
        var observable = Observable.interval(milliSeconds, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
        if (owner != null) {
            observable = observable.compose(
                AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle()
            )
        }
        observable.subscribe(object : Observer<Long> {
            override fun onSubscribe(disposable: Disposable) {
                mDisposable = disposable
            }

            override fun onNext(number: Long) {
                rxAction?.invoke(number)
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    val isCanceled: Boolean
        get() = mDisposable!!.isDisposed

    /**
     * 取消订阅
     */
    fun cancel() {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }

}