package com.creditease.monitor.mybatis.sqllite.grafana.po;

public class MonitorTask {
    private Integer id;

    private String taskName;


    private String status;

    private String cutTemplate;

    private String dataSourceIp;

    private Integer dataSourcePort;

    private String dataSourceLog;

    private String createTime;

    private String updateTime;

    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getCutTemplate() {
        return cutTemplate;
    }

    public void setCutTemplate(String cutTemplate) {
        this.cutTemplate = cutTemplate == null ? null : cutTemplate.trim();
    }

    public String getDataSourceIp() {
        return dataSourceIp;
    }

    public void setDataSourceIp(String dataSourceIp) {
        this.dataSourceIp = dataSourceIp == null ? null : dataSourceIp.trim();
    }

    public Integer getDataSourcePort() {
        return dataSourcePort;
    }

    public void setDataSourcePort(Integer dataSourcePort) {
        this.dataSourcePort = dataSourcePort;
    }

    public String getDataSourceLog() {
        return dataSourceLog;
    }

    public void setDataSourceLog(String dataSourceLog) {
        this.dataSourceLog = dataSourceLog == null ? null : dataSourceLog.trim();
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime == null ? null : updateTime.trim();
    }
}