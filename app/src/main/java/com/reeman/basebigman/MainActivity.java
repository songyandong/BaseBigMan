package com.reeman.basebigman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.reeman.basebigman.constant.MyEvent;
import com.reeman.basebigman.manager.ChargeManager;
import com.reeman.basebigman.manager.NerveManager;
import com.reeman.basebigman.process.SpeechRecoProcess;
import com.reeman.basebigman.process.SpeechResultProcess;
import com.reeman.nerves.RobotActionProvider;
import com.speech.processor.SpeechPlugin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int ACTION_UPDATE_VOL = 0;
    public static final int ACTION_SPEECH_VALUE = 1;
    public static final int ACTION_SPEECH_RESULT = 2;

    @BindView(R.id.fm_iv_vol)
    ImageView mIvVol;
    @BindView(R.id.fm_tv_response_result)
    TextView mSpeechResult;
    @BindView(R.id.fm_tv_speech_value)
    TextView mTvSpeechValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        initView();
        init();
        initFilter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        SpeechPlugin.getInstance().startRecognize();    //打开录音开关
        RobotActionProvider.getInstance().setBeam(0);
        NerveManager.getInstance().init();  //初始化外设
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        SpeechPlugin.getInstance().stopRecognize();
        SpeechPlugin.getInstance().stopSpeak();
        NerveManager.getInstance().uninit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void init() {
        SpeechPlugin.CreateInstance(this);  //init ai
        Log.e("MainActivity",
                "===:" + (SpeechPlugin.getInstance() == null) + " / " + (RobotActionProvider
                        .getInstance() == null));
        SpeechPlugin.getInstance().setDevID(RobotActionProvider.getInstance().getRobotID());
        SpeechPlugin.getInstance().setRecognizeListener(new SpeechRecoProcess());  // 设置识别处理
        SpeechPlugin.getInstance().setResultProcessor(new SpeechResultProcess());    // 设置结果处理
        RobotActionProvider.getInstance().setBeam(0);//设置8mic拾音方向

        //        SpeechPlugin.getInstance().setViewSpeakListener(null);      //设置语音合成（文字转语音）回调
        // 合成被打断，合成开始，合成结束
        //        SpeechPlugin.getInstance().setAIScene("main");    //讯飞语料场景设置
        //        SpeechPlugin.getInstance().setRecognizeZonn("hotel-main|商务机器人闲聊"); //按优先级设置知识库

    }

    private void initFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("REEMAN_BROADCAST_WAKEUP");
        filter.addAction("REEMAN_BROADCAST_SCRAMSTATE");
        filter.addAction("REEMAN_LAST_MOVTION");
        filter.addAction("ACTION_POWER_CONNECTE_REEMAN");
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction("AUTOCHARGE_ERROR_DOCKNOTFOUND");
        filter.addAction("AUTOCHARGE_ERROR_DOCKINGFAILURE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "=====receiver:" + action);
            if ("REEMAN_BROADCAST_WAKEUP".equals(action)) { //唤醒广播
                RobotActionProvider.getInstance().setBeam(0);
                SpeechPlugin.getInstance().startRecognize();
                int angle = intent.getIntExtra("REEMAN_8MIC_WAY", 0);   //唤醒角度
                // 进行唤醒处理
                NerveManager.getInstance().wakeUp(angle);
            } else if ("REEMAN_BROADCAST_SCRAMSTATE".equals(action)) { //急停开关状态监听广播
                int stopState = intent.getIntExtra("SCRAM_STATE", -1);
                NerveManager.stopState = stopState;
            } else if ("REEMAN_LAST_MOVTION".equals(action)) {      //运动结束广播
                int type = intent.getIntExtra("REEMAN_MOVTION_TYPE", 0); // 16,17,18 前左右
            } else if ("ACTION_POWER_CONNECTE_REEMAN".equals(action) || Intent
                    .ACTION_BATTERY_CHANGED.equals(
                    action) || "AUTOCHARGE_ERROR_DOCKNOTFOUND".equals(
                    action) || "AUTOCHARGE_ERROR_DOCKINGFAILURE".equals(action))    //电量，充电相关广播
            {
                ChargeManager.getInstance().batteryUpdate(intent);
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) { //网络状态改变

            }
        }
    };



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MyEvent.MainEvent event) {
        Log.i(TAG, "====update===action:" + event.action);
        switch (event.action) {
            case ACTION_UPDATE_VOL:
                int vol = (int) event.data;
                setVol(vol);
                break;
            case ACTION_SPEECH_VALUE:
                String value = (String) event.data;
                setSpeechValue(value);
                break;
            case ACTION_SPEECH_RESULT:
                String result = (String) event.data;
                setSpeechResult(result);
                SpeechPlugin.getInstance().startSpeak(result);
                break;
        }
    }

    /**
     * 语音音量大小变化
     * @param vol
     */
    public void setVol (int vol) {
        Log.e("NerveManager", "====:" + (mIvVol == null) + " / " + vol);
        int v = vol / 3;
        switch (v) {
            case 1:
                mIvVol.setImageResource(R.drawable.rec_vol1);
                break;
            case 2:
                mIvVol.setImageResource(R.drawable.rec_vol2);
                break;
            case 3:
                mIvVol.setImageResource(R.drawable.rec_vol3);
                break;
            case 4:
                mIvVol.setImageResource(R.drawable.rec_vol4);
                break;
            case 5:
                mIvVol.setImageResource(R.drawable.rec_vol5);
                break;
            case 6:
                mIvVol.setImageResource(R.drawable.rec_vol6);
                break;
            default:
                mIvVol.setImageResource(R.drawable.rec_vol1);
                break;
        }
    }

    /**
     * 语音听写结果
     * @param text
     */
    public void setSpeechResult (String text) {
        mSpeechResult.setText(text);
    }

    /**
     * 语义理解结果
     * @param text
     */
    public void setSpeechValue (String text) {
        mTvSpeechValue.setText(text);
    }

}
