package com.creditease.monitor.mybatis.sqllite.grafana.po;

import java.util.Date;

public class MonitorApplication {

    /******************start ??????*****************/
    private String projectName;
    private String machineName;
    /******************end ??????*****************/

    private Integer id;

    private Integer projectId;


    private Integer machineId;

    private String applicationName;

    private Byte applicationType;

    private String applicationDetailParam;

    private String applicationDesc;


    private Date createTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
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
        this.applicationDesc = applicationDesc == null ? null : applicationDesc.trim();
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

}