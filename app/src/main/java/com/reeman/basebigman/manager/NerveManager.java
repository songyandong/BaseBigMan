package com.reeman.basebigman.manager;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.reeman.basebigman.ReemanApp;
import com.reeman.basebigman.constant.MyEvent;
import com.reeman.basebigman.constant.SceneValue;
import com.reeman.basebigman.presenter.MainPresenter;
import com.reeman.basebigman.process.RosProcess;
import com.reeman.nerves.RobotActionProvider;
import com.rsc.aidl.OnPrintListener;
import com.rsc.impl.RscServiceConnectionImpl;
import com.rsc.reemanclient.ConnectServer;
import com.speech.processor.SpeechPlugin;
import com.synjones.idcard.IDCardInfo;
import com.synjones.idcard.OnIDListener;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ye on 2017/11/8.
 */

public class NerveManager {
    private static final String TAG = NerveManager.class.getSimpleName();
    public static final int MSG_BATTERY_WAKEUP = 0;

    private volatile static NerveManager instance;
    private ConnectServer mConnectServer;
    public static int stopState = 0;
    public static boolean isMoving;
    public static int chargeState = ChargeManager.STATE_NORMAL;
    public static String mSceneType = SceneValue.SCENE_NORMAL;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BATTERY_WAKEUP:
                    turnByAngle(msg.arg1);
                    break;
            }
        }
    };

    public static NerveManager getInstance () {
        if (instance == null) {
            synchronized (NerveManager.class) {
                if (instance == null) {
                    instance = new NerveManager();
                }
            }
        }
        return instance;
    }

    private NerveManager () {
        int se = RobotActionProvider.getInstance().getScramState();
        stopState = se;
    }

    public void init () {
        // 连接外设
        mConnectServer = ConnectServer.getInstance(ReemanApp.getInstance(), connection);
        mConnectServer.registerROSListener(new RosProcess());   //设置外设的监听回调(物体识别，人体检测，导航等回调)
    }

    public void uninit () {
        if (mConnectServer != null) {
            mConnectServer.release();
            mConnectServer = null;
        }
    }

    private RscServiceConnectionImpl connection = new RscServiceConnectionImpl() {
        public void onServiceConnected (int name) {
            if (mConnectServer == null)
                return;
            if (name == ConnectServer.Connect_D3) {
                // 3D摄像头回调
                //                mConnectServer.register3DSensorListener(sensorListener);
            } else if (name == ConnectServer.Connect_Pr_Id) {
                // 身份证识别回调
                mConnectServer.registerIDListener(idListener);
            }
        }

        public void onServiceDisconnected (int name) {
            System.out.println("onServiceDisconnected......");
        }
    };


    /**
     * 身份证识别回调
     */
    private OnIDListener idListener = new OnIDListener.Stub() {
        @Override
        public void onResult (IDCardInfo idCardInfo, byte[] bytes) throws RemoteException {
            Log.e(TAG,
                    "name: " + idCardInfo.getName() + ",nation: " + idCardInfo.getNation() + ",birthday: " +
                            idCardInfo.getBirthday() + ",sex: " + idCardInfo.getSex() + ",address: " + idCardInfo
                            .getAddress() + ",append: " + idCardInfo.getAppendAddress() + ",fpname: " + idCardInfo
                            .getFpName() + ",grantdept: " + idCardInfo.getGrantdept() + ",idcardno: " + idCardInfo
                            .getIdcardno() + ",lifebegin: " + idCardInfo.getUserlifebegin() + "," + "lifeend: " +
                            idCardInfo.getUserlifeend());
        }
    };

    /**
     * 打印
     *
     * @param s               打印机文本，不可为空
     * @param type            打印机指令，0， 打印模板内容；1，打印自定义模板内容；2，上传bmp图片 (说明)
     * @param onPrintListener 打印机回调，返回打印机状态码，状态码见文档说明
     */
    public void print (String s, int type, OnPrintListener onPrintListener) {
        if (mConnectServer != null) {
            Log.v("ReemanSdk", "客户端申请打印");
            mConnectServer.onPrint(
                    "{\"data\":[{\"iNums\":\"1\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"【XXXXX支行】\"," +
                            "\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"欢迎您光临\",\"alignmen\":\"1\"," +
                            "\"changerow\":\"0\"},{\"text\":\"Welcome to CCB\",\"alignmen\":\"1\",\"feedline\":\"3\"," +
                            "" + "\"changerow\":\"0\"},{\"text\":\"【F888】\",\"alignmen\":\"1\",\"feedline\":\"2\"," +
                            "\"changerow\":\"0\",\"bold\":\"1\",\"sizetext\":\"1\"}," +
                            "{\"text\":\"前面有：【XX】人，请稍候：【XX】clients ahead of you\",\"alignmen\":\"0\"," +
                            "\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"尊敬的：【" + s + "】客户\"," +
                            "\"alignmen\":\"0\",\"changerow\":\"0\"},{\"text\":\"您将要办理：【XX】\",\"alignmen\":\"0\"," +
                            "\"changerow\":\"0\"},{\"text\":\"【打印日期，精确到秒】\",\"alignmen\":\"0\",\"feedline\":\"2\"," +
                            "\"changerow\":\"0\"},{\"text\":\"不向陌生人汇款、转账，谨防上当！\",\"alignmen\":\"0\"," +
                            "\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"温馨提示：您是我行优质客户，诚邀你办理我行信用卡。\"," +
                            "\"alignmen\":\"0\",\"feedline\":\"5\",\"changerow\":\"0\",\"cutpaper\":\"0\"}]}",
                    1, onPrintListener);
        } else {
            Log.v("ReemanSdk", "服务未绑定");
        }
    }

    /**
     * 检测人体回调
     * @param result
     */
    public void handlerPt(String result) {
        if (result.length() < 4)
            return;
        String buf = result.substring(4, result.length() - 1);
        String[] data = buf.split(",");
        if (data.length == 3) {
            float x = Float.valueOf(data[0]);
            float y = Float.valueOf(data[1]);
            float z = Float.valueOf(data[2]);

            if (z <= 0) {
                //TODO 没检测到人
            } else if (z <= 1.5) {
                //TODO 人与机器小于等于1.5米
            } else if (z <= 2) {
                //TODO 人与机器小于等于2米
            } else {
                //TODO 没有检测到人
            }
        }
    }


    /**
     * 唤醒处理
     *
     * @param angle 唤醒角度
     */
    public void wakeUp (int angle) {
        //        mSceneType = SceneValue.SCENE_NORMAL;
        Log.e(TAG, "===wakeup:" + mSceneType);
        if (chargeState == ChargeManager.STATE_CHARGE) {
            SpeechPlugin.getInstance().startSpeak("你好，正在适配器充电中，有什么可以帮到你？");
            return;
        } else if (chargeState == ChargeManager.STATE_AUTO_CHARGE) {
            //正在充电 判断时间，判断电量
            if (ChargeManager.batterLevel > 30) {
                //                RobotActionProvider.getInstance().sendRosCom("bat:uncharge");
                mSceneType = SceneValue.SCENE_NORMAL; // 暂停充电
                chargeState = ChargeManager.STATE_NORMAL;
                RobotActionProvider.getInstance().moveFront(20, 0);
                Message msg = new Message();
                msg.what = MSG_BATTERY_WAKEUP;
                msg.arg1 = angle;
                mHandler.sendMessageDelayed(msg, 2500);
            } else {
                if (angle < 20 || angle > 340) {
                    SpeechPlugin.getInstance().startSpeak("你好，有什么可以帮到你？");
                } else {
                    SpeechPlugin.getInstance().startSpeak("你好，小曼当前电量低正在充电，有什么事可以到我前面说哦");
                }
            }
            return;
        }

        if (SceneValue.SCENE_NAVIGATION.equals(mSceneType)) {
            RobotActionProvider.getInstance().sendRosCom("cancel_goal");
            turnByAngle(angle);
            mSceneType = SceneValue.SCENE_NORMAL;
        } else if (SceneValue.SCENE_BATTERY_CONNECTING.equals(mSceneType)) {
            // 正在连接充电桩  判断电量
            mSceneType = SceneValue.SCENE_NORMAL;
            RobotActionProvider.getInstance().sendRosCom("bat:uncharge");
            Message msg = new Message();
            msg.what = MSG_BATTERY_WAKEUP;
            msg.arg1 = angle;
            mHandler.sendMessageDelayed(msg, 1000);
        } else if (SceneValue.SCENE_BATTERY_NAVIGATION.equals(mSceneType)) {
            mSceneType = SceneValue.SCENE_NORMAL;
            RobotActionProvider.getInstance().sendRosCom("cancel_goal");
            turnByAngle(angle);
        } else {
            mSceneType = SceneValue.SCENE_NORMAL;
            turnByAngle(angle);
        }
        SpeechPlugin.getInstance().startSpeak("你好，小曼在呢");
    }


    /**
     * 唤醒角度调整
     *
     * @param angle
     */
    public void turnByAngle (int angle) {
        if (stopState == 0) {
            SpeechPlugin.getInstance().startSpeak("你也好啊，吃过了吗");
            return;
        }
        if (chargeState == 1) {
            SpeechPlugin.getInstance().startSpeak("你好啊，我在呢");
            return;
        } else if (chargeState == 2) {
            if (ChargeManager.batterLevel < 20) {
                SpeechPlugin.getInstance().startSpeak("你好，小曼当前电量低正在充电，有什么事可以到我前面说哦");
            } else {
                RobotActionProvider.getInstance().moveFront(20, 0);
                Message msg = new Message();
                msg.what = MSG_BATTERY_WAKEUP;
                msg.arg1 = angle;
                mHandler.sendMessageDelayed(msg, 2500);
            }
        }
        //        wakeUpState = true;
        if (10 <= angle && angle < 180) {
            RobotActionProvider.getInstance().moveLeft(angle, 0);
        } else if (180 <= angle && angle <= 350) {
            RobotActionProvider.getInstance().moveRight(360 - angle, 0);
        }
    }

    /**
     * AI语音回答结果更新ui
     *
     * @param result
     */
    public void updateSpeechResultUi (String result) {
        EventBus.getDefault().post(new MyEvent.MainEvent(MainPresenter.ACTION_SPEECH_RESULT, result));
    }

}
