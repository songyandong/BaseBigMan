package com.reeman.basebigman.manager;

import com.reeman.nerves.RobotActionProvider;

/**
 * Created by ye on 2017/12/15.
 * 硬件控制使用
 */

public class ActionManager {
    private volatile static ActionManager instance;

    public static ActionManager getInstance () {
        if (instance == null) {
            synchronized (ActionManager.class) {
                if (instance == null) {
                    instance = new ActionManager();
                }
            }
        }
        return instance;
    }

    /**
     * 前进
     *
     * @param param 距离，单位厘米，取值范围大于1
     */
    private void moveFront (int param) {
        //第二参数为速度，暂不支持控制
        RobotActionProvider.getInstance().moveFront(param, 0);
    }


    /**
     * 后退
     *
     * @param param 距离，单位厘米，取值范围大于1
     */
    private void moveBack (int param) {
        //第二参数为速度，暂不支持控制
        RobotActionProvider.getInstance().moveBack(param, 0);
    }


    /**
     * 左转
     *
     * @param param 角度，取值范围1~360度
     */
    private void moveLeft (int param) {
        //第二参数为速度，暂不支持控制
        RobotActionProvider.getInstance().moveLeft(param, 0);
    }


    /**
     * 右转
     *
     * @param param 角度，取值范围1~360度
     */
    private void moveRight (int param) {
        //第二参数为速度，暂不支持控制
        RobotActionProvider.getInstance().moveRight(param, 0);
    }

    /**
     * 停止前后左右运动
     */
    private void moveStop () {
        RobotActionProvider.getInstance().stopMove();
    }


    /**
     * 耳朵灯控制
     *
     * @param codeP 1,load; 2,listen; 3,sport; 0xff,关闭
     */
    private void setEarLight (int codeP) {
        RobotActionProvider.getInstance().earControlTtyS4(codeP);
    }

    /**
     * 眼睛灯控制
     *
     * @param codeP 1,lightblue;2,yellow;3,purple;4,white
     *              5,blue;6,green;7,red;8,redfresh; 0x0b,charge;0xff,关闭
     */
    private void setEyeLight (int codeP) {
        RobotActionProvider.getInstance().eyeControlTtyS4(codeP);
    }

    /**
     * 头部运动控制
     *
     * @param codeT 1，上；2，下；3，左；4，右
     * @param angle 运动角度， 上(最大6°) 下(最大20°)
     * @param speed 运动速度 取值范围1到100
     */
    private void setHeadMove (int codeT, int angle, int speed) {
        RobotActionProvider.getInstance().headControlTtyS4(codeT, angle, speed);
    }

    /**
     * 获取头部当前位置
     *
     * @return 长度2；[0]，垂直方向： 正数为正，负数为下
     *               [1]，水平方向： 正数为左，负数为右
     */
    private int[] getHeadPosition () {
        return RobotActionProvider.getInstance().getHeadPosition();
    }

    /**
     * 组合动作控制
     *
     * @param type 1睡眠状态，2活动筋骨，3苏醒 4伸懒腰，5打招呼，
     *             6环视观众 7邀请，8低头检查，9欢迎 10高兴，11害羞，12激动
     *             13自主状态，14头部复位，15头部左右复位，16头部上下复位，17手臂复位
     *             18左臂复位，19右臂复位，20头部手臂复位，21头部停止
     */
    private void sendActionGroup (int type) {
        RobotActionProvider.getInstance().combinedActionTtyS4(type);
    }

    /**
     * 主动获取急停开关状态
     *
     * @return 0按下，1打开
     */
    private int getScramState () {
        return RobotActionProvider.getInstance().getScramState();
    }




}
