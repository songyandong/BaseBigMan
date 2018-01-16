package com.reeman.basebigman.speech;

import android.text.TextUtils;
import android.util.Log;

import com.reeman.basebigman.speech.domain.IflyEntity;
import com.reeman.basebigman.manager.NerveManager;
import com.reeman.basebigman.speech.base.SpeechHandler;

/**
 * Created by ye on 2017/11/8.
 * 处理讯飞返回结果
 */

public class IflySpeech implements SpeechHandler {

    @Override
    public void handlerSpeech (String json) {
        Log.e("SpeechHandler", "ifly====:" + json);
        if (json == null || json.length() < 1) return;
        IflyEntity iflyEntity = null;
        try {
            iflyEntity = gson.fromJson(json, IflyEntity.class);
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (iflyEntity != null && iflyEntity.answer != null && !TextUtils.isEmpty(iflyEntity.answer.text)) {
            String result = iflyEntity.answer.text;
            NerveManager.getInstance().updateSpeechResultUi(result);
        } else {
            //没有答案

        }
    }
}
