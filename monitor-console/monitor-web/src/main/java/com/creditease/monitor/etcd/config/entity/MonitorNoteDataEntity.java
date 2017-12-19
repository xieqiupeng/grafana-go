package com.creditease.monitor.etcd.config.entity;

import java.util.List;

public class MonitorNoteDataEntity {
    //类型
    private String type;
    //日志路径
    private List<String> path;
    //IP地址
    private List<String> ipAddress;
    //切割规则存储
    private String cleanRule;

    private List<String> tomcatHostPort;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public List<String> getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(List<String> ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCleanRule() {
        return cleanRule;
    }

    public void setCleanRule(String cleanRule) {
        this.cleanRule = cleanRule;
    }

    public List<String> getTomcatHostPort() {
        return tomcatHostPort;
    }

    public void setTomcatHostPort(List<String> tomcatHostPort) {
        this.tomcatHostPort = tomcatHostPort;
    }
}
