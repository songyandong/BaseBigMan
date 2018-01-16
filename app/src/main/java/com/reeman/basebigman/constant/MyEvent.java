package com.reeman.basebigman.constant;

/**
 * Created by ye on 2017/11/8.
 */

public class MyEvent {
    public static class MainEvent {
        public int action;
        public Object data;
        public MainEvent(int action, Object obj) {
            this.action = action;
            this.data = obj;
        }
    }
}
