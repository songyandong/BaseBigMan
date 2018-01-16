package com.reeman.basebigman.process;

import android.util.Log;

import com.iflytek.cloud.SpeechError;
import com.reeman.basebigman.constant.MyEvent;
import com.reeman.basebigman.presenter.MainPresenter;
import com.speech.abstracts.IRecognizeListener;
import com.speech.processor.SpeechPlugin;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ye on 2017/11/10.
 * 语音识别监听处理
 */

public class SpeechRecoProcess implements IRecognizeListener {
    private static final String TAG = SpeechRecoProcess.class.getSimpleName();

    @Override
    public void onBeginOfSpeech () {

    }

    @Override
    public void onError (SpeechError speechError) {
        int code = speechError.getErrorCode();
        Log.v(TAG, "onError: " + code);
        Log.v(TAG, "error cause: " + speechError.getErrorDescription());
        if (code == 10114) {
            SpeechPlugin.getInstance().startSpeak("网络连接异常！");
        } else if (code == 20001) {
            SpeechPlugin.getInstance().startSpeak("网络未连接！");
        }
    }

    @Override
    public void onEndOfSpeech () {

    }

    @Override
    public void onResult (String s) {
        //语音识别结果返回
        EventBus.getDefault().post(new MyEvent.MainEvent(MainPresenter.ACTION_SPEECH_VALUE, s));
    }

    @Override
    public void onVolumeChanged (int i, byte[] bytes) {
        //语音音量大小返回
        Log.e(TAG, "====onVolumeChanged==:" + i);
        EventBus.getDefault().post(new MyEvent.MainEvent(MainPresenter.ACTION_UPDATE_VOL, i));
    }
}
