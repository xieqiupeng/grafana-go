package com.creditease.monitor.constant;

public class MonitorTaskConstant {

    /**
     * 监控任务状态
     */
    public static class MonitorTaskStatus{
        /**启动*/
        public static final byte START = 0;
        /**暂停*/
        public static final byte PAUSE = 1;
    }

    public static class MonitorTomcatServer{
        /**监控*/
        public static final byte YES = 0;
        /**不监控*/
        public static final byte NO = 1;
    }

}
