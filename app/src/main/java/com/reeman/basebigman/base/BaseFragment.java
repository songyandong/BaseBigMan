package com.reeman.basebigman.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by ye on 2017/11/8.
 */

public abstract class BaseFragment<V,P extends BasePresenter<V>> extends Fragment{
    protected P mPresenter;

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        mPresenter.attachView((V)this);
    }

    @Override
    public void onStart () {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    public void onStop () {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        mPresenter.detachView();
    }

    protected abstract P createPresenter();
}
