package com.creditease.monitor.etcd.entity;

import java.util.List;

public class MonitorNoteDataEntity {
    //类型
    private String type;
    //日志路径
    private List<String> path;
    //IP地址
    private List<MonitorService> services;

    public static class MonitorService{

        private String host;

        private List<ServerTypeParam> serverTypeParams;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public List<ServerTypeParam> getServerTypeParams() {
            return serverTypeParams;
        }

        public void setServerTypeParams(List<ServerTypeParam> serverTypeParams) {
            this.serverTypeParams = serverTypeParams;
        }
    }

    /**
     * 服务类型端口
     */
    public static class ServerTypeParam{
        /**服务类型 参照:MonitorTaskConstant.MonitorServerType*/
        private int type;
        /**服务端口*/
        private List<String> param;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<String> getParam() {
            return param;
        }

        public void setParam(List<String> param) {
            this.param = param;
        }
    }

    //切割规则存储
    private String cleanRule;

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

    public String getCleanRule() {
        return cleanRule;
    }

    public void setCleanRule(String cleanRule) {
        this.cleanRule = cleanRule;
    }

    public List<MonitorService> getServices() {
        return services;
    }

    public void setServices(List<MonitorService> services) {
        this.services = services;
    }
}
