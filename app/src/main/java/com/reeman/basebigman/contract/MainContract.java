package com.reeman.basebigman.contract;

import com.reeman.basebigman.base.BaseView;

/**
 * Created by ye on 2017/11/8.
 */

public interface MainContract {

    interface View extends BaseView {
        void setVol(int vol);
        void setSpeechResult(String text);
        void setSpeechValue(String text);
    }

    interface Presenter {

    }
}
