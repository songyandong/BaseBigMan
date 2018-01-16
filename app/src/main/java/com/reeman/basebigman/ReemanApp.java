package com.reeman.basebigman;

import android.support.multidex.MultiDexApplication;

import com.reeman.basebigman.manager.NerveManager;
import com.speech.processor.SpeechPlugin;

/**
 * Created by ye on 2017/11/8.
 */

public class ReemanApp extends MultiDexApplication {
//    public static final String FACE_COMPANY_APPID="EEjrqcmMsDPercU4";
    public static final String FACE_COMPANY_APPID="MNtKoND7UQGQ1CNf";

    public static ReemanApp mInstance;

    @Override
    public void onCreate () {
        super.onCreate();
        mInstance = this;
        SpeechPlugin.CreateSpeechUtility(this, "586b9487", FACE_COMPANY_APPID);
        NerveManager.getInstance();
    }

    public static ReemanApp getInstance() {
        return mInstance;
    }
}
