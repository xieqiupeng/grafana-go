package com.creditease.monitor.enums;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/13,at 10:27
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */

/**
 * 启动/暂停 枚举类
 */
public enum MonitorTaskStatus {

    START("0","启动"), PAUSE("1","暂停");
    private String value;
    private String msg;
    MonitorTaskStatus(String value,String msg){
        this.value=value;
        this.msg=msg;
    }

    public String getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }

}
