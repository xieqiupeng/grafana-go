package com.creditease.monitor.vo;

public class EditMonitorTaskVo {

    private String taskName;

    private String cutTemplate;

    private String dataSourceServerIp;

    private String dataSourceLog;

    private Byte isMonitorTomcatServer;

    private String tomcatServerHost;

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

    public String getDataSourceServerIp() {
        return dataSourceServerIp;
    }

    public void setDataSourceServerIp(String dataSourceServerIp) {
        this.dataSourceServerIp = dataSourceServerIp;
    }

    public String getDataSourceLog() {
        return dataSourceLog;
    }

    public void setDataSourceLog(String dataSourceLog) {
        this.dataSourceLog = dataSourceLog;
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
        this.tomcatServerHost = tomcatServerHost;
    }
}
