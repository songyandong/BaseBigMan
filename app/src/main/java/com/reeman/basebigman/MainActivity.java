package com.reeman.basebigman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.reeman.basebigman.fragment.MainFragment;
import com.reeman.basebigman.manager.ChargeManager;
import com.reeman.basebigman.manager.NerveManager;
import com.reeman.basebigman.process.SpeechRecoProcess;
import com.reeman.basebigman.process.SpeechResultProcess;
import com.reeman.nerves.RobotActionProvider;
import com.speech.processor.SpeechPlugin;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private MainFragment mMainFragment;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        init();
        initFilter();
    }

    @Override
    protected void onStart () {
        super.onStart();
        SpeechPlugin.getInstance().startRecognize();    //打开录音开关
        RobotActionProvider.getInstance().setBeam(0);
        NerveManager.getInstance().init();  //初始化外设
    }

    @Override
    protected void onStop () {
        super.onStop();
        SpeechPlugin.getInstance().stopRecognize();
        SpeechPlugin.getInstance().stopSpeak();
        NerveManager.getInstance().uninit();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void init () {
        SpeechPlugin.CreateInstance(this);  //init ai
        Log.e("MainActivity",
                "===:" + (SpeechPlugin.getInstance() == null) + " / " + (RobotActionProvider.getInstance() == null));
        SpeechPlugin.getInstance().setDevID(RobotActionProvider.getInstance().getRobotID());
        SpeechPlugin.getInstance().setRecognizeListener(new SpeechRecoProcess());  // 设置识别处理
        SpeechPlugin.getInstance().setResultProcessor(new SpeechResultProcess());    // 设置结果处理
        RobotActionProvider.getInstance().setBeam(0);//设置8mic拾音方向

        //        SpeechPlugin.getInstance().setViewSpeakListener(null);      //设置语音合成（文字转语音）回调 合成被打断，合成开始，合成结束
        //        SpeechPlugin.getInstance().setAIScene("main");    //讯飞语料场景设置
        //        SpeechPlugin.getInstance().setRecognizeZonn("hotel-main|商务机器人闲聊"); //按优先级设置知识库

    }

    private void initFilter () {
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
        public void onReceive (Context context, Intent intent) {
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
            } else if ("ACTION_POWER_CONNECTE_REEMAN".equals(action) || Intent.ACTION_BATTERY_CHANGED.equals(
                    action) || "AUTOCHARGE_ERROR_DOCKNOTFOUND".equals(
                    action) || "AUTOCHARGE_ERROR_DOCKINGFAILURE".equals(action))    //电量，充电相关广播
            {
                ChargeManager.getInstance().batteryUpdate(intent);
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) { //网络状态改变

            }
        }
    };


    private void initView () {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mMainFragment == null) {
            mMainFragment = MainFragment.newInstance();
            transaction.add(R.id.am_fl_content, mMainFragment);
        }
        transaction.commit();
    }
}
