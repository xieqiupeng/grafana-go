package com.creditease.monitor.service;

import com.alibaba.fastjson.JSON;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.creditease.monitor.constant.MonitorTaskConstant;
import com.creditease.monitor.etcd.entity.MonitorNoteDataEntity;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 监控任务针对ETCD的操作
 */
@Service
public class MonitorTaskEtcdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorTaskEtcdService.class);

    @Autowired
    private Client etcdClient;

    private static final String MONITOR_TASK_DIR = "/monitor";
    private static final String CHAR_START = "/";

    //
    public boolean upSert(MonitorTask monitorTask) {
//        if (monitorTask != null) {
//            String key = checkAndGetEtcdKey(monitorTask.getTaskName());
//            String cutTemplate = monitorTask.getCutTemplate();
//            String dataSourceServerIp = monitorTask.getDataSourceServerIp();
//            String dataSourceLog = monitorTask.getDataSourceLog();
//            checkNull(cutTemplate);
//            checkNull(dataSourceServerIp);
//            checkNull(dataSourceLog);
//            MonitorNoteDataEntity noteDataEntity = new MonitorNoteDataEntity();
//            noteDataEntity.setType("log");
//            noteDataEntity.setCleanRule(cutTemplate);
//            String[] pathArr = dataSourceLog.split(MonitorTaskConstant.comma);
//            noteDataEntity.setPath(Arrays.asList(pathArr));
//            String[] dataSourceServerIpArr = dataSourceServerIp.split(MonitorTaskConstant.comma);
//            LinkedHashSet<String> ips = new LinkedHashSet<>();
//            for (String ip : dataSourceServerIpArr) {
//                ips.add(ip);
//            }
//            //存储HOST,类型，对应IP
//            Map<String, Map<Integer, List<Integer>>> map = new HashMap<>();
//            Map<String, List<Integer>> ipPorts = new HashMap<>();
//            ips.forEach(ip -> {
//                map.put(ip, new HashMap<>());
//                ipPorts.put(ip, new ArrayList<>());
//            });
//            //
//            String tomcatServerHost = monitorTask.getTomcatServerHost();
//            if (monitorTask.getIsMonitorTomcatServer() != null
//                    && MonitorTaskConstant.MonitorTomcatServer.YES == monitorTask.getIsMonitorTomcatServer()
//                    && StringUtils.isNotBlank(tomcatServerHost)) {
//                String[] ipPortsArray = tomcatServerHost.split(MonitorTaskConstant.comma);
//                for (String ipPortStr : ipPortsArray) {
//                    String[] ipPort = ipPortStr.split(MonitorTaskConstant.colon);
//                    if (ipPort.length > 1) {
//                        String ip = ipPort[0];
//                        if (ipPorts.containsKey(ip)) {
//                            int port = Integer.parseInt(ipPort[1]);
//                            List<Integer> ports = ipPorts.get(ip);
//                            if (!ports.contains(port)) {
//                                List<Integer> list = map.get(ip).get(MonitorTaskConstant.MonitorServerType.tomcatServer);
//                                if (list == null) {
//                                    list = new ArrayList<>();
//                                    map.get(ip).put(MonitorTaskConstant.MonitorServerType.tomcatServer, list);
//                                }
//                                list.add(port);
//                                ports.add(port);
//                            } else {
//                                LOGGER.info("host={},port={}重复", ip, port);
//                            }
//                        }
//                    }
//                }
//            }
//            if (map.isEmpty()) {
//                LOGGER.info("监控节点没有host");
//                return false;
//            }
//            //
//            List<MonitorNoteDataEntity.MonitorService> monitorServices = new ArrayList<>();
//            for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
//                String host = it.next();
//                List<MonitorNoteDataEntity.ServerTypeParam> serverTypePorts = new ArrayList<>();
//                Map<Integer, List<Integer>> listMap = map.get(host);
//                for (Iterator<Integer> ite = listMap.keySet().iterator(); ite.hasNext(); ) {
//                    Integer type = ite.next();
//                    List<Integer> ports = listMap.get(type);
//                    List<String> params = new ArrayList<>();
//                    ports.forEach(port -> {
//                        StringBuffer buffer = new StringBuffer(host);
//                        buffer.append(MonitorTaskConstant.colon);
//                        buffer.append(port);
//                        params.add(buffer.toString());
//                    });
//                    MonitorNoteDataEntity.ServerTypeParam serverTypePort = new MonitorNoteDataEntity.ServerTypeParam();
//                    serverTypePort.setType(type);
//                    serverTypePort.setParam(params);
//                    serverTypePorts.add(serverTypePort);
//                }
//                MonitorNoteDataEntity.MonitorService monitorService = new MonitorNoteDataEntity.MonitorService();
//                monitorService.setHost(host);
//                monitorService.setServerTypeParams(serverTypePorts);
//                monitorServices.add(monitorService);
//            }
//            noteDataEntity.setServices(monitorServices);
//            //
//            String str = JSON.toJSONString(noteDataEntity);
//            //
//            try {
//                etcdClient.getKVClient()
//                        .put(ByteSequence.fromString(key),
//                                ByteSequence.fromString(str))
//                        .get();
//                return true;
//            } catch (InterruptedException | ExecutionException e) {
//                LOGGER.error("upSert error monitorTask={},errorMsg",
//                        JSON.toJSONString(noteDataEntity),
//                        e.getMessage());
//            }
//        }
        return false;
    }

    public boolean delete(String monitorTaskName) {
        String key = checkAndGetEtcdKey(monitorTaskName);
        try {
            etcdClient.getKVClient()
                    .delete(ByteSequence.fromString(key))
                    .get();
        } catch (InterruptedException | ExecutionException e1) {
            LOGGER.info("delete key={} error,msg={}", key, e1.getMessage());
            return false;
        }
        return true;
    }

    public MonitorNoteDataEntity select(String monitorTaskName) throws Exception {
        if (StringUtils.isNotBlank(monitorTaskName)) {
            String key = checkAndGetEtcdKey(monitorTaskName);
            GetResponse response = etcdClient.getKVClient()
                    .get(ByteSequence.fromString(key))
                    .get();
            String nodeValue = response.getKvs()
                    .get(0)
                    .getValue()
                    .toStringUtf8();
            if (StringUtils.isNotBlank(nodeValue)) {
                return JSON.parseObject(nodeValue, MonitorNoteDataEntity.class);
            }
        }
        return null;
    }

    private void checkNull(String str) {
        if (StringUtils.isBlank(str)) {
            throw new NullPointerException();
        }
    }

    private String checkAndGetEtcdKey(String key) {
        checkNull(key);
        StringBuffer buffer = new StringBuffer(MONITOR_TASK_DIR);
        if (!key.startsWith(CHAR_START)) {
            buffer.append(CHAR_START);
        }
        buffer.append(key);
        return buffer.toString();
    }
}
