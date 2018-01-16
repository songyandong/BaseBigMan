package com.reeman.basebigman.speech.base;

import com.reeman.basebigman.speech.IflySpeech;
import com.reeman.basebigman.speech.ReemanSpeech;

/**
 * Created by ye on 2017/11/8.
 */

public class SpeechFactory {
    public static final int TYPE_IFLY = 2;  //讯飞结果
    public static final int TYPE_REEMAN = 1;    //锐曼结果

    public static SpeechHandler createSpeech (int type) {
        SpeechHandler speechHandler = null;
        switch (type) {
            case TYPE_IFLY:
                speechHandler = new IflySpeech();
                break;
            case TYPE_REEMAN:
                speechHandler = new ReemanSpeech();
                break;
            default:
                break;
        }
        return speechHandler;
    }

}
