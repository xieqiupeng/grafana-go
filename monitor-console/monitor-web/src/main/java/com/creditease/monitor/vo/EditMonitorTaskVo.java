package com.creditease.monitor.vo;

public class EditMonitorTaskVo {

    private Integer projectId;

    private String machineId;

    private String taskName;

    private String cutTemplate;

    private String dataSourceLog;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCutTemplate() {
        return cutTemplate;
    }

    public void setCutTemplate(String cutTemplate) {
        this.cutTemplate = cutTemplate;
    }

    public String getDataSourceLog() {
        return dataSourceLog;
    }

    public void setDataSourceLog(String dataSourceLog) {
        this.dataSourceLog = dataSourceLog;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }
}
