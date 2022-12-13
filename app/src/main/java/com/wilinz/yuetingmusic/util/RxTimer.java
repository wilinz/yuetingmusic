package com.wilinz.yuetingmusic.util;


import androidx.lifecycle.LifecycleOwner;

import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class RxTimer {

    private Disposable mDisposable;

    public void timer(long milliSeconds, final RxAction rxAction) {
        timer(null, milliSeconds, rxAction);
    }

    /**
     * milliseconds毫秒后执行指定动作
     *
     * @param milliSeconds
     * @param rxAction
     */
    public void timer(LifecycleOwner owner, long milliSeconds, final RxAction rxAction) {
        Observable<Long> observable = Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread());
        if (owner != null) {
            observable = observable.compose(AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle());
        }
        observable.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                mDisposable = disposable;
            }

            @Override
            public void onNext(@NonNull Long number) {
                if (rxAction != null) {
                    rxAction.action(number);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                //取消订阅
                cancel();
            }

            @Override
            public void onComplete() {
                //取消订阅
                cancel();
            }
        });
    }

    public void interval(long milliSeconds, final RxAction rxAction) {
        interval(null, milliSeconds, rxAction);
    }

    /**
     * 每隔milliseconds毫秒后执行指定动作
     *
     * @param milliSeconds
     * @param rxAction
     */
    public void interval(LifecycleOwner owner, long milliSeconds, final RxAction rxAction) {
        Observable<Long> observable = Observable.interval(milliSeconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread());

        if (owner != null) {
            observable = observable.compose(AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle());
        }

        observable.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                mDisposable = disposable;
            }

            @Override
            public void onNext(@NonNull Long number) {
                if (rxAction != null) {
                    rxAction.action(number);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public boolean isCanceled(){
        return mDisposable.isDisposed();
    }
    /**
     * 取消订阅
     */
    public void cancel() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    public interface RxAction {
        /**
         * 让调用者指定指定动作
         *
         * @param number
         */
        void action(long number);
    }
}