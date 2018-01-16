package com.reeman.basebigman.speech;

import android.text.TextUtils;
import android.util.Log;

import com.reeman.basebigman.domain.BaseBigManEntity;
import com.reeman.basebigman.manager.ChargeManager;
import com.reeman.basebigman.manager.NavigationManager;
import com.reeman.basebigman.manager.NerveManager;
import com.reeman.basebigman.speech.base.SpeechHandler;
import com.reeman.basebigman.speech.domain.ReemanEntity;

/**
 * Created by ye on 2017/11/8.
 * 处理锐曼返回结果
 */

public class ReemanSpeech implements SpeechHandler {

    @Override
    public void handlerSpeech (String json) {
        Log.e("SpeechHandler", "reeman====:" + json);
        if (json == null || json.length() < 1)
            return;
        ReemanEntity reemanEntity = null;
        try {
            reemanEntity = gson.fromJson(json, ReemanEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reemanEntity != null && !TextUtils.isEmpty(reemanEntity.Data)) {
            String result = reemanEntity.Data;
            //根据知识库的不同做相应的处理
            switch (reemanEntity.Msg) {
                case "商务机器人闲聊": //知识库名称
                    NerveManager.getInstance().updateSpeechResultUi(result);
                    break;
                case "baseBigMan":
                    BaseBigManEntity baseBigManEntity = null;
                    try {
                        baseBigManEntity = gson.fromJson(result, BaseBigManEntity.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handlerBaseBigMan(baseBigManEntity);
                    break;
                case "导航":
                    handlerNavigation(result);
                    break;
            }
        } else {
            //没有答案
        }
    }


    /**
     * 语音控制进行导航
     * @param result
     */
    public void handlerNavigation (String result) {
        String locationName = "";
        if (result.contains("back_")) {
            locationName = "前台";
        } else if ("cancel_charge".equals(result)) {
            ChargeManager.getInstance().cancelCharge();
            return;
        } else if (result.contains("charge_")) {
            ChargeManager.getInstance().goCharge("好的，那我充电去啦");
            return;
        } else {
            locationName = parseNavigation(result);
            if (TextUtils.isEmpty(locationName)) {
                return;
            }
        }
        NavigationManager.getInstance().navigationByName(locationName);
    }

    private String parseNavigation (String result) {
        if (result.contains("charge_") || result.contains("navigation_")) {
            return result.substring(result.indexOf("_") + 1, result.length());
        }
        return "";
    }

    public void handlerBaseBigMan (BaseBigManEntity baseBigManEntity) {
        if (baseBigManEntity == null) {
            return;
        }
        switch (baseBigManEntity.service) {
            case "cmd":
                break;
            case "navigation":
                break;
        }
    }

}
