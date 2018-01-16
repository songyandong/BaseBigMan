package com.reeman.basebigman.speech.domain;

/**
 * Created by ye on 2017/11/9.
 */

public class IflyEntity {

    /**
     * answer : {"answerType":"openQA","emotion":"default","question":{"question":"你好你好你叫什么名字","question_ws":"你好/VI//
     * 你好/VI//  你/NP//  叫/NN//  什么/AD//  名字/NN//"},"text":"我就是我，是颜色不一样的烟火。","topicID":"32184203558031559","type":"T"}
     * man_intv :
     * no_nlu_result : 0
     * operation : ANSWER
     * rc : 0
     * service : openQA
     * status : 0
     * text : 你好你好你叫什么名字
     * uuid : atn001bb0b1@ch74900d5f54896f2601
     * sid : sch3bfff700@gz3c3e0d5f54893c3e00
     */

    public AnswerBean answer;
    public String man_intv;
    public int no_nlu_result;
    public String operation;
    public int rc;
    public String service;
    public int status;
    public String text;
    public String uuid;
    public String sid;

    public static class AnswerBean {
        /**
         * answerType : openQA
         * emotion : default
         * question : {"question":"你好你好你叫什么名字","question_ws":"你好/VI//  你好/VI//  你/NP//  叫/NN//  什么/AD//  名字/NN//"}
         * text : 我就是我，是颜色不一样的烟火。
         * topicID : 32184203558031559
         * type : T
         */

        public String answerType;
        public String emotion;
        public QuestionBean question;
        public String text;
        public String topicID;
        public String type;

        public static class QuestionBean {
            /**
             * question : 你好你好你叫什么名字
             * question_ws : 你好/VI//  你好/VI//  你/NP//  叫/NN//  什么/AD//  名字/NN//
             */

            public String question;
            public String question_ws;
        }
    }
}
