package com.creditease.consumers.message;

import com.alibaba.fastjson.JSONObject;
import com.creditease.consumers.concurrent.BlockPolicy;
import com.creditease.consumers.dataclean.DataCleanEntity;
import com.creditease.consumers.dataclean.IDataClean;
import com.creditease.consumers.influxdb.InfluxdbManager;
import com.creditease.consumers.influxdb.InfluxdbPo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AyncBizLogMessageHandle implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(AyncBizLogMessageHandle.class);

    private IDataClean dataClean;
    private InfluxdbManager manager;
    //这里异步处理消息 最多只能同时多少批次的消息
    private ExecutorService executorService;
    public AyncBizLogMessageHandle(InfluxdbManager manager, IDataClean dataClean,int threadPoolSize) {
        this.dataClean = dataClean;
        this.manager = manager;
        executorService = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),new BlockPolicy());
    }
    @Override
    public boolean handleMessage(List<String> messages) {
        return ayncHandleMessage(messages);
    }
    public boolean ayncHandleMessage(List<String> messages){
        executorService.execute(()-> {
            try {
                List<InfluxdbPo> pos = new ArrayList<>();
                long t1 = System.currentTimeMillis();
                messages.forEach(msg->{
                    log.info("接收msg={},开始处理",msg);
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(msg);
                        //日志收集时间
                        Date date = jsonObject.getDate("@timestamp");
                        //数据源名称
                        String monitorName = jsonObject.getJSONObject("fields").getString("tag");
                        //服务器hostName
                        String hostName = jsonObject.getJSONObject("beat").getString("hostname");
                        //业务数据
                        String monitorData = jsonObject.getString("message");
                        if(StringUtils.isBlank(monitorData)){
                            log.info("业务数据为空 不处理 monitorName={},hostName={}",monitorName,hostName);
                            return;
                        }
                        List<DataCleanEntity> list = dataClean.clean(monitorName,monitorData);
                        InfluxdbPo po = new InfluxdbPo();
                        if(list != null && !list.isEmpty()){
                            list.forEach(entity->{
                                if(entity.getValue() != null){
                                    if(entity.isIndex()){
                                        po.getTags().put(entity.getName(),entity.getValue().toString());
                                    }else{
                                        po.getFields().put(entity.getName(),entity.getValue());
                                    }

                                }
                            });
                        }else{
                            log.info("msg={},无法按照切割规则切割",msg);
                        }
                        if(!po.getFields().isEmpty()){
                            po.setMeasurement(monitorName);
                            po.setCreateTime(date == null ? System.currentTimeMillis() : date.getTime());
                            if(StringUtils.isNotBlank(hostName)){
                                po.getTags().put("host_name",hostName);
                            }
                            pos.add(po);
                        }else{
                            log.info("msg={},没有Field属性,丢弃",msg);
                        }
                    }catch (Exception e){
                        log.error("单条 msg={},数据处理失败,errorMsg={}",msg,e.getMessage());
                    }
                });
                if(!pos.isEmpty()){
                    manager.insertPoint(pos);
                }
                long t2 = System.currentTimeMillis();
                log.info("批量处理{}条message耗时={}.ms",messages.size(),(t2-t1));
            }catch (Exception e){
                log.error("msg={},数据处理失败,errorMsg={}",messages,e.getMessage());
            }
        });
        return true;
    }
}
