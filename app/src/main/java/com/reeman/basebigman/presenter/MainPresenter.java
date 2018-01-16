package com.reeman.basebigman.presenter;

import android.util.Log;

import com.reeman.basebigman.base.BasePresenter;
import com.reeman.basebigman.constant.MyEvent;
import com.reeman.basebigman.contract.MainContract;
import com.reeman.basebigman.manager.NerveManager;
import com.reeman.nerves.RobotActionProvider;
import com.speech.processor.SpeechPlugin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by ye on 2017/11/8.
 */

public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
    private static final String TAG = MainPresenter.class.getSimpleName();
    public static final int ACTION_UPDATE_VOL = 0;
    public static final int ACTION_SPEECH_VALUE = 1;
    public static final int ACTION_SPEECH_RESULT = 2;

    private MainContract.View mMainView;


    public MainPresenter(MainContract.View mainView) {
        this.mMainView = mainView;
    }

    @Override
    public void onStart () {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop () {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MyEvent.MainEvent event) {
        Log.i(TAG, "====update===action:" + event.action);
        switch (event.action) {
            case ACTION_UPDATE_VOL:
                int vol = (int) event.data;
                mMainView.setVol(vol);
                break;
            case ACTION_SPEECH_VALUE:
                String value = (String) event.data;
                mMainView.setSpeechValue(value);
                break;
            case ACTION_SPEECH_RESULT:
                String result = (String) event.data;
                mMainView.setSpeechResult(result);
                SpeechPlugin.getInstance().startSpeak(result);
                break;
        }
    }


    @Override
    public void start () {
        NerveManager.stopState = RobotActionProvider.getInstance().getScramState();
    }


}
