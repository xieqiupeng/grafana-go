package com.creditease.monitor.mybatis.sqllite.grafana.po;

import java.util.Date;
public class MonitorApplication {
    private Integer id;

    private Integer machineId;

    private String applicationName;

    private Byte applicationType;

    private String applicationDetailParam;

    private String applicationDesc;

    private Byte status;

    private Date createTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMachineId() {
        return machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName == null ? null : applicationName.trim();
    }

    public Byte getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(Byte applicationType) {
        this.applicationType = applicationType;
    }

    public String getApplicationDetailParam() {
        return applicationDetailParam;
    }

    public void setApplicationDetailParam(String applicationDetailParam) {
        this.applicationDetailParam = applicationDetailParam == null ? null : applicationDetailParam.trim();
    }

    public String getApplicationDesc() {
        return applicationDesc;
    }

    public void setApplicationDesc(String applicationDesc) {
        this.applicationDesc = applicationDesc;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}