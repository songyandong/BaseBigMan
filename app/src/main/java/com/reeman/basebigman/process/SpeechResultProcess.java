package com.reeman.basebigman.process;

import com.reeman.basebigman.speech.base.SpeechFactory;
import com.reeman.basebigman.speech.base.SpeechHandler;
import com.speech.abstracts.IResultProcessor;
import com.speech.bean.ReemanResult;

/**
 * Created by ye on 2017/11/10.
 * 语音结果监听回调处理
 */

public class SpeechResultProcess implements IResultProcessor {

    @Override
    public void onPartialResult (ReemanResult reemanResult) {
        //语音处理回答结果返回
        if (reemanResult == null)
            return;
        String json = reemanResult.getJson();
        int type = reemanResult.getType();
        if (json == null) {

            return;
        }
        SpeechHandler speechHandler = SpeechFactory.createSpeech(type);
        if (speechHandler != null) {
            speechHandler.handlerSpeech(json);
        } else {
            //答案为空
        }
    }
}
