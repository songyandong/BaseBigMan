package com.reeman.basebigman.process;

import android.util.Log;

import com.reeman.basebigman.constant.SceneValue;
import com.reeman.basebigman.manager.NavigationManager;
import com.reeman.basebigman.manager.NerveManager;
import com.rsc.impl.OnROSListener;
import com.speech.processor.SpeechPlugin;

/**
 * Created by ye on 2017/11/10.
 * 外设监听回调(物体识别，人体检测，导航状态回调，充电信息回调...)
 */

public class RosProcess extends OnROSListener {
    private static final String TAG = RosProcess.class.getSimpleName();

    @Override
    public void onResult (String result) {
        Log.e(TAG, "----OnROSListener.onResult()---result:" + result);
        if (result != null) {
            if (result.startsWith("od:")) {
                //Log.e(TAG, "收到物体识别回调：  " + result);
            } else if (result.startsWith("pt:[")) {
                //人体检测结果回调
                NerveManager.getInstance().handlerPt(result);
            } else if (result.startsWith("move_status:")) {
                Log.e(TAG, "收到导航信息回调：  " + result);
                NavigationManager.getInstance().navigationUpdate(result);
            } else if (result.equals("bat:reached")) {
                //Log.e(TAG, "收到充电信息回调：  " + result);
                SpeechPlugin.getInstance().startSpeak("到达充电区域，开始连接充电桩");
                NerveManager.mSceneType = SceneValue.SCENE_BATTERY_CONNECTING;
            } else if (result.equals("sys:uwb:0")) {
                //Log.e(TAG, "收到导航信息回调：  uwb错误：:" + result);
            }
        }
    }
}
