package com.reeman.basebigman.manager;

import android.text.TextUtils;
import android.util.Log;

import com.reeman.basebigman.constant.SceneValue;
import com.reeman.nerves.RobotActionProvider;
import com.speech.processor.SpeechPlugin;

/**
 * Created by ye on 2017/11/10.
 * 导航相关处理
 */

public class NavigationManager {
    private static final String TAG = NavigationManager.class.getSimpleName();
    private volatile static NavigationManager instance;


    public static NavigationManager getInstance() {
        if (instance == null) {
            synchronized (NerveManager.class) {
                if (instance == null) {
                    instance = new NavigationManager();
                }
            }
        }
        return instance;
    }

    /**
     * 根据坐标控制机器人执行导航
     * @param point 传入参数格式：goal:nav[0.05,0.1,-28.0]
     */
    public void navigation(String point) {
        RobotActionProvider.getInstance().sendRosCom(point);
    }


    /**
     * 根据名称、姓名进行导航
     * @param name  地点名称
     */
    public void navigationByName (String name) {
        Log.e(TAG, "===navigation:" + name);
        if (TextUtils.isEmpty(name))
            return;
        if (NerveManager.stopState == 0) {
            SpeechPlugin.getInstance().startSpeak("急停开关被按下，无法进行导航");
            return;
        }
        if (SpeechPlugin.getInstance().getContactLocations() == null) {
            SpeechPlugin.getInstance().startSpeak("我还没设置导航地址呢");
            return;
        }
        //获取地点：~/reeman/data/locations.cfg 文件中获取
        String location = SpeechPlugin.getInstance().getContactLocations().get(name);
        if (TextUtils.isEmpty(location)) {
            SpeechPlugin.getInstance().startSpeak("不好意思，我还不知道" + name + "在哪呢");
            return;
        }
        String point = "";
        String scenetype = NerveManager.mSceneType;
        if ("充电站".equals(name)) {
            //前去充电
            scenetype = SceneValue.SCENE_BATTERY_NAVIGATION;
            point = coordinate(location, 0);
        } else {
            scenetype = SceneValue.SCENE_NAVIGATION;
            point = coordinate(location, 1);
        }
        if (TextUtils.isEmpty(point)) {
            SpeechPlugin.getInstance().startSpeak("不好意思，我还不知道" + name + "在哪呢");
            return;
        }
        SpeechPlugin.getInstance().setCurrentNavPoint(name);
        NerveManager.mSceneType = scenetype;
        RobotActionProvider.getInstance().sendRosCom(point);
    }


    /**
     * 处理导航回调
     * @param result move_status:x = ?  0 : 静止待命   1 : 上次目标失败，等待新的导航命令   2 : 上次目标完成，等待新的导航命令  
     *               3 : 移动中，正在前往目的地   4 : 前方障碍物   5 : 目的地被遮挡 6：用户取消导航 7：收到新的导航
     */
    public void navigationUpdate(String result) {
        switch (result) {
            case "move_status:0":
                NerveManager.isMoving = false;
                break;
            case "move_status:1":
                NerveManager.isMoving = false;
                NerveManager.mSceneType = SceneValue.SCENE_NORMAL;
                SpeechPlugin.getInstance().startSpeak("导航失败");
                break;
            case "move_status:2":
                NerveManager.mSceneType = SceneValue.SCENE_NORMAL;
                NerveManager.isMoving = false;
                String mNavLocation = SpeechPlugin.getInstance().getCurrentNavPoint();
                StringBuilder tip = new StringBuilder();
                if (TextUtils.isEmpty(mNavLocation)) {
                    mNavLocation = "目的地";
                    tip.append(mNavLocation).append("到了，请问还有什么需要帮忙的吗？");
                    SpeechPlugin.getInstance().startSpeak(tip.toString());
                } else if ("充电站".equals(mNavLocation)) {
                    tip.append(mNavLocation).append("到了，开始对接充电桩");
                    SpeechPlugin.getInstance().startSpeak(tip.toString());
                    SpeechPlugin.getInstance().setCurrentNavPoint("");
                } else if (mNavLocation.contains("前台")) {
                    RobotActionProvider.getInstance().combinedActionTtyS4(32);
                    SpeechPlugin.getInstance().startSpeak("这里就是前台了");
                } else {
                    RobotActionProvider.getInstance().combinedActionTtyS4(32);
                    SpeechPlugin.getInstance().startSpeak("这里是" + mNavLocation);
                }
                break;
            case "move_status:3":
                NerveManager.isMoving = true;
                break;
            case "move_status:4":
                break;
            case "move_status:5":
                break;
            case "move_status:6":
                NerveManager.isMoving = false;
                NerveManager.mSceneType = SceneValue.SCENE_NORMAL;
                break;
            case "move_status:7":
                break;
            default:
                break;
        }
    }


    /**
     *  根据名称获取坐标
     * @param location 地点
     * @param type  类型  0:充电 1:正常导航
     * @return 返回具体坐标
     */
    public String coordinate(String location, int type) {
        StringBuffer buffer = new StringBuffer();
        if (TextUtils.isEmpty(location))
            return "";
        String[] point = location.split(",");
        float x = Float.valueOf(point[0]);
        float y = Float.valueOf(point[1]);
        float yaw = Float.valueOf(point[2]);
        if (type == 0) {
            buffer.append("goal:charge");
        } else if (type == 1) {
            buffer.append("goal:nav");
        }
        buffer.append("[").append(x).append(",").append(y).append(",").append(yaw).append("]");
        return buffer.toString();
    }

}
