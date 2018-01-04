package com.creditease.monitor.constant;

public class MonitorTaskConstant {

    public static final String comma = ",";

    public static final String colon = ":";

    /**
     * 监控任务状态
     */
    public static class MonitorTaskStatus{
        /**启动*/
        public static final Byte START = 0;
        /**暂停*/
        public static final Byte PAUSE = 1;
    }

    public static class MonitorTomcatServer{
        /**监控*/
        public static final Byte YES = 0;
        /**不监控*/
        public static final Byte NO = 1;
    }

    /***
     * 监控服务类型
     */
    public static class MonitorServerType{
        /**tomcat服务*/
        public static final Integer tomcatServer = 1;
    }

}
