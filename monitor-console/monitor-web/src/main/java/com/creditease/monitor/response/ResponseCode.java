package com.creditease.monitor.response;

import com.creditease.response.ResultMessage;

public class ResponseCode {
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

}
