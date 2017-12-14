package com.creditease.monitor.mybatis.sqllite.grafana.po;

import java.util.Date;

public class MonitorTask {
    private Integer id;

    private String taskName;

    private Byte status;

    private String cutTemplate;

    private String dataSourceServerIp;

    private String dataSourceLog;

    private Byte isMonitorTomcatServer;

    private String tomcatServerHost;

    private Date createTime;

    private Date updateTime;

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

    public String getDataSourceServerIp() {
        return dataSourceServerIp;
    }

    public void setDataSourceServerIp(String dataSourceServerIp) {
        this.dataSourceServerIp = dataSourceServerIp == null ? null : dataSourceServerIp.trim();
    }

    public String getDataSourceLog() {
        return dataSourceLog;
    }

    public void setDataSourceLog(String dataSourceLog) {
        this.dataSourceLog = dataSourceLog == null ? null : dataSourceLog.trim();
    }

    public Byte getIsMonitorTomcatServer() {
        return isMonitorTomcatServer;
    }

    public void setIsMonitorTomcatServer(Byte isMonitorTomcatServer) {
        this.isMonitorTomcatServer = isMonitorTomcatServer;
    }

    public String getTomcatServerHost() {
        return tomcatServerHost;
    }

    public void setTomcatServerHost(String tomcatServerHost) {
        this.tomcatServerHost = tomcatServerHost == null ? null : tomcatServerHost.trim();
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