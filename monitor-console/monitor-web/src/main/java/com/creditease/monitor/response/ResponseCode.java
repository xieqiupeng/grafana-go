package com.creditease.monitor.response;

import com.creditease.response.ResultMessage;

public class ResponseCode {

    /********************************数据源*******************************/
    @ResultMessage("数据源名称已经存在")
    public static final int DATA_SOURCE_HAS_EXISTS = 10001;

    @ResultMessage("启动监控数据源异常")
    public static final int START_TASK_ERROR = 10002;

    @ResultMessage("删除监控数据源异常")
    public static final int PAUSE_TASK_ERROR = 10003;

    @ResultMessage("监控数据源不存在")
    public static final int DATA_SOURCE_NOT_EXISTS = 10004;

    @ResultMessage("监控数据源处于启动状态无法操作")
    public static final int DATA_SOURCE_IS_STARTING = 10005;

    @ResultMessage("无效的IP地址:{0}")
    public static final int INVALID_IP = 10006;

    @ResultMessage("重复IP:{0}")
    public static final int IP_REPEAT = 10007;

    @ResultMessage("无效的tomcat地址:{0}")
    public static final int INVALID_TOMCAT_ADDRESS = 10008;

    @ResultMessage("存在重复的tomcat地址:{0}")
    public static final int TOMCAT_ADDRESS_REPEAT = 10009;

    @ResultMessage("监控服务地址和服务器地址IP不一致:{0}")
    public static final int IP_HAS_DIFFER = 10010;

    @ResultMessage("无效端口:{0}")
    public static final int INVALID_PORT = 10011;

    /********************************项目*******************************/
    @ResultMessage("项目名称已经存在")
    public static final int PROJECT_NAME_HAS_EXISTS = 20001;

    @ResultMessage("某机器正在引用此项目导致无法操作")
    public static final int PROJECT_IS_REFERRED_BY_MACHINE_LIST = 20002;

    @ResultMessage("此项目不存在")
    public static final int PROJECT_NOT_EXISTS = 20003;

    /********************************机器*******************************/
    @ResultMessage("机器名称已经存在")
    public static final int MACHINE_NAME_HAS_EXISTS = 30001;

    @ResultMessage("某应用正在引用此机器导致无法操作")
    public static final int MACHINE_IS_REFERRED_BY_APPLICATION = 30002;

    @ResultMessage("某任务正在引用此机器导致无法操作")
    public static final int MACHINE_IS_REFERRED_BY_TASK = 30003
            ;
    @ResultMessage("此项目不存在")
    public static final int MACHINE_NOT_EXISTS = 30004;

    /********************************应用*******************************/
    @ResultMessage("应用名称已经存在")
    public static final int APPLICATION_NAME_HAS_EXISTS = 40001;

    @ResultMessage("此应用不存在")
    public static final int APPLICATION_NOT_EXISTS = 40002;

}
