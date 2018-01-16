package com.reeman.basebigman.manager;

import android.content.Intent;
import android.util.Log;

import com.iflytek.cloud.SpeechError;
import com.reeman.basebigman.constant.SceneValue;
import com.reeman.basebigman.util.LogUtils;
import com.reeman.nerves.RobotActionProvider;
import com.speech.abstracts.ISpeakListener;
import com.speech.processor.SpeechPlugin;

/**
 * Created by luke on 2017/3/2.
 * 充电相关类
 */

public class ChargeManager {
    public static final int STATE_NORMAL = 0;   //未充电
    public static final int STATE_CHARGE = 1;   //适配器充电中
    public static final int STATE_AUTO_CHARGE = 2;  //充电桩充电中

    private volatile static ChargeManager instance;
    private static final String TAG = ChargeManager.class.getSimpleName();
    private static boolean notFindCharge = false;
    public static int batterLevel= 100;
    private int dockNotFound;

    public static ChargeManager getInstance () {
        if (instance == null) {
            synchronized (ChargeManager.class) {
                if (instance == null) {
                    instance = new ChargeManager();
                }
            }
        }
        return instance;
    }


    public void batteryUpdate (Intent intent) {
        if (intent == null) {
            Log.v("ResultExecutor", "intent = null!");
            return;
        }
        String action = intent.getAction();
        LogUtils.i(TAG, "===action:" + action);
        if ("ACTION_POWER_CONNECTE_REEMAN".equals(action)) {
            // 插入充电器
            int powcon = intent.getIntExtra("POWERCHARGE", 0);
            Log.v("ResultExecutor", "powcom: " + powcon);
            if (powcon == 0) {
                // 拔掉充电器
                NerveManager.chargeState = STATE_NORMAL;
            } else if (powcon == 1) {
                NerveManager.chargeState = STATE_CHARGE;
                SpeechPlugin.getInstance().startSpeak("连接电源适配器成功，开始充电");
            } else if (powcon == 2) {
                dockNotFound = 0;
                NerveManager.chargeState = STATE_AUTO_CHARGE;
                notFindCharge = false;
                SpeechPlugin.getInstance().startSpeak("连接充电桩成功，开始充电");
            }
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            batterLevel = intent.getIntExtra("level", 0);

            LogUtils.i(TAG, "===change:" + batterLevel + " / " + NerveManager.chargeState);
            if (NerveManager.chargeState == STATE_AUTO_CHARGE || NerveManager.chargeState == STATE_CHARGE)
                return;
            if (batterLevel <= 15) {
                // 未找到充电桩
                if (notFindCharge || NerveManager.mSceneType == SceneValue.SCENE_BATTERY_NAVIGATION ||
                        NerveManager.mSceneType == SceneValue.SCENE_BATTERY_CONNECTING) {
                    return;
                }
                SpeechPlugin.getInstance().startSpeak("电量太低，我偷偷充电去了，嘿嘿");
                NavigationManager.getInstance().navigationByName("充电站");
            }
        } else if ("AUTOCHARGE_ERROR_DOCKNOTFOUND".equals(action)) {
            // 未找到充电桩
            Log.w("executor", "未找到充电桩：  " + dockNotFound);
            dockNotFound++;
            if (dockNotFound < 3) {
                SpeechPlugin.getInstance().startSpeak("未找到充电桩，我再试一次！", new ISpeakListener() {
                    @Override
                    public void onSpeakOver (SpeechError speechError) {
                        goCharge("");
                    }

                    @Override
                    public void onInterrupted () {
                        goCharge("");
                    }

                    @Override
                    public void onSpeakBegin (String s) {

                    }
                });
            } else {
                Log.w("executor", "dock not found 连续找不到充电桩超过2次" + dockNotFound);
                dockNotFound = 0;
                notFindCharge = true;
                SpeechPlugin.getInstance().startSpeak("小曼找不到充电桩啦，相关信息已存档。");
            }
        } else if ("AUTOCHARGE_ERROR_DOCKINGFAILURE".equals(action)) {
            // 连接充电桩失败
            Log.w("executor", "连接充电桩失败：  " + dockNotFound);
            dockNotFound++;
            if (dockNotFound < 3) {
                SpeechPlugin.getInstance().startSpeak("连接充电桩失败，我再试一次！", new ISpeakListener() {
                    @Override
                    public void onSpeakOver (SpeechError speechError) {
                        goCharge("");
                    }

                    @Override
                    public void onInterrupted () {
                        goCharge("");
                    }

                    @Override
                    public void onSpeakBegin (String s) {

                    }
                });
            } else {
                Log.w("executor", "dock not found 连接充电桩失败超过2次" + dockNotFound);
                dockNotFound = 0;
                notFindCharge = true;
                SpeechPlugin.getInstance().startSpeak("小曼连接不上充电桩啦，相关信息已存档。");
            }
        }
    }


    public void goCharge (String speak) {
        switch (NerveManager.chargeState) {
            case STATE_NORMAL:
                if (batterLevel > 95) {
                    SpeechPlugin.getInstance().startSpeak("电量充足，不用充电哦");
                    return;
                }
                if (NerveManager.stopState == 0) {
                    SpeechPlugin.getInstance().startSpeak("急停开关被按下，无法去充电");
                    return;
                }
                SpeechPlugin.getInstance().startSpeak(speak);
                NavigationManager.getInstance().navigationByName("充电站");
                break;
            case STATE_CHARGE:
                SpeechPlugin.getInstance().startSpeak("现在正在适配器充电呢");
                break;
            case STATE_AUTO_CHARGE:
                SpeechPlugin.getInstance().startSpeak("现在正在充电桩上充电呢");
                break;
            default:
                break;
        }
    }

    public void cancelCharge () {
        int chargeStatus = NerveManager.chargeState;
        switch (chargeStatus) {
            case STATE_CHARGE:
                SpeechPlugin.getInstance().startSpeak("现在正在适配器充电,无法取消充电");
                break;
            case STATE_AUTO_CHARGE:
                if (batterLevel < 25) {
                    SpeechPlugin.getInstance().startSpeak("电量不足，还要继续充电哦");
                    return;
                }
                RobotActionProvider.getInstance().moveFront(20, 0);
                break;
        }
        SpeechPlugin.getInstance().startSpeak("好的");
        if (SceneValue.SCENE_BATTERY_CONNECTING.equals(NerveManager.mSceneType)) {
            RobotActionProvider.getInstance().sendRosCom("bat:uncharge");
            NerveManager.mSceneType = SceneValue.SCENE_NORMAL;
        }
    }
}
