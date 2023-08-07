package com.app.freya.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Admin
 *Date 2023/6/5
 */
open class SingleLiveEvent<T> : MutableLiveData<T> () {

    private val TAG = "SingleLiveEvent"

    private val mPending = AtomicBoolean(false)


    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if(hasActiveObservers()){

        }
        super.observe(owner
        ) { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }

    }


    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }


    @MainThread
    fun call() {
        value = null
    }

}