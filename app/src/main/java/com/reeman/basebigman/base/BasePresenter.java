package com.reeman.basebigman.base;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by ye on 2017/11/8.
 */

public abstract class BasePresenter<V> {
    protected Reference<V> mViewRef;//View 接口类型的弱引用

    public void attachView (V view) {
        mViewRef = new WeakReference<V>(view);
    }

    protected V getView () {
        return mViewRef.get();
    }

    public boolean isViewAttached () {
        return mViewRef != null && mViewRef.get() != null;
    }

    public void detachView () {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    public void onStart(){}
    public void onStop() {}

    public abstract void start ();

}
