package com.creditease.monitor.mybatis.sqllite.grafana.po;

import java.util.Date;

public class MonitorTask2 {
    private Integer id;

    private Integer projectId;

    private String taskName;

    private String dataSourceLog;

    private String projectMachineId;

    private Byte status;

    private String cutTemplate;

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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public String getDataSourceLog() {
        return dataSourceLog;
    }

    public void setDataSourceLog(String dataSourceLog) {
        this.dataSourceLog = dataSourceLog == null ? null : dataSourceLog.trim();
    }

    public String getProjectMachineId() {
        return projectMachineId;
    }

    public void setProjectMachineId(String projectMachineId) {
        this.projectMachineId = projectMachineId == null ? null : projectMachineId.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getCutTemplate() {
        return cutTemplate;
    }

    public void setCutTemplate(String cutTemplate) {
        this.cutTemplate = cutTemplate == null ? null : cutTemplate.trim();
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