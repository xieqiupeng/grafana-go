package com.creditease.monitor.service;

import com.alibaba.fastjson.JSON;
import com.creditease.monitor.etcd.config.entity.MonitorNoteDataEntity;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdErrorCode;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 监控任务针对ETCD的操作
 */
@Service
public class MonitorTaskEtcdService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorTaskEtcdService.class);

    private static final String separator = ",";

    @Autowired
    private EtcdClient etcdClient;

    public static final String monitorTaskDir  = "/monitor";

    public boolean upSert(MonitorTask monitorTask){
        if(monitorTask != null){
            String key = checkAndGetEtcdKey(monitorTask.getTaskName());
            String cutTemplate = monitorTask.getCutTemplate();
            String dataSourceServerIp =  monitorTask.getDataSourceServerIp();
            String dataSourceLog = monitorTask.getDataSourceLog();
            checkNull(cutTemplate);
            checkNull(dataSourceServerIp);
            checkNull(dataSourceLog);
            MonitorNoteDataEntity noteDataEntity = new MonitorNoteDataEntity();
            noteDataEntity.setType("log");
            noteDataEntity.setCleanRule(cutTemplate);
            String[] pathArr = dataSourceLog.split(separator);
            noteDataEntity.setPath(Arrays.asList(pathArr));
            String[] dataSourceServerIpArr = dataSourceServerIp.split(separator);
            noteDataEntity.setIpAddress(Arrays.asList(dataSourceServerIpArr));
            String tomcatServerHost = monitorTask.getTomcatServerHost();
            if(StringUtils.isNotBlank(tomcatServerHost)){
                noteDataEntity.setTomcatHostPort(Arrays.asList(tomcatServerHost.split(separator)));
            }
            try {
                String str = JSON.toJSONString(noteDataEntity);
                EtcdResponsePromise<EtcdKeysResponse> responsePromise = etcdClient.put(key,str).send();
                responsePromise.get();
                return true;
            }catch (Exception e){
                logger.error("upSert error monitorTask={},errorMsg",JSON.toJSONString(noteDataEntity),e.getMessage());
                return false;
            }
        }
        return false;
    }

    public boolean delete(String monitorTaskName){
        String key = checkAndGetEtcdKey(monitorTaskName);
        try {
            EtcdResponsePromise<EtcdKeysResponse> responsePromise = etcdClient.delete(key).send();
            responsePromise.get();
            return true;
        }catch (EtcdException e){
            if(e.getErrorCode() == EtcdErrorCode.KeyNotFound){
                return true;
            }
            logger.info("delete key={} error,msg={}",key,e.getMessage());
            return false;
        }catch (Exception e){
            logger.info("delete key={} error,msg={}",key,e.getMessage());
            return false;
        }
    }

    public MonitorNoteDataEntity select(String monitorTaskName) throws Exception {
        if(StringUtils.isNotBlank(monitorTaskName)){
            String key = checkAndGetEtcdKey(monitorTaskName);
            try {
                EtcdResponsePromise<EtcdKeysResponse> responsePromise = etcdClient.get(key).send();
                EtcdKeysResponse response = responsePromise.get();
                String nodeValue = response.getNode().getValue();
                if(StringUtils.isNotBlank(nodeValue)){
                    return JSON.parseObject(nodeValue,MonitorNoteDataEntity.class);
                }
            }catch (EtcdException e){
                if(e.getErrorCode() == EtcdErrorCode.KeyNotFound){
                    return null;
                }
               throw e;
            }
        }
        return  null;
    }

    private void checkNull(String str){
        if(StringUtils.isBlank(str)){
            throw new NullPointerException();
        }
    }

    private String checkAndGetEtcdKey(String key){
        checkNull(key);
        StringBuffer buffer = new StringBuffer(monitorTaskDir);
        if(!key.startsWith("/")){
            buffer.append("/");
        }
        buffer.append(key);
        return buffer.toString();
    }
}
