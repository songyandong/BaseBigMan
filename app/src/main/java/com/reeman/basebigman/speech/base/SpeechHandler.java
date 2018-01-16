package com.reeman.basebigman.speech.base;

import com.google.gson.Gson;

/**
 * Created by ye on 2017/11/8.
 */

public interface SpeechHandler {
    Gson gson = new Gson();
    void handlerSpeech(String json);
}
