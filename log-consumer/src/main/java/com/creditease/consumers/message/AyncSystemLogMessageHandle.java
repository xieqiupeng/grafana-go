package com.creditease.consumers.message;

import com.alibaba.fastjson.JSONObject;
import com.creditease.consumers.concurrent.BlockPolicy;
import com.creditease.consumers.influxdb.InfluxdbManager;
import com.creditease.consumers.influxdb.InfluxdbPo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 系统日志收集
 */
public class AyncSystemLogMessageHandle implements MessageHandler{

    private static final Logger logger = LoggerFactory.getLogger(AyncSystemLogMessageHandle.class);
    /**浮点类型*/
    private static final String type_float = "_float";
    /**字符串类型*/
    private static final String type_string = "_string";
    /**double类型*/
    private static final String type_double = "_double";
    /**long类型*/
    private static final String type_long = "_long";
    /**TAG标识*/
    private static final String TAG="_tag";
    /**FIELD标识*/
    private static final String FIELD = "_field";

    private InfluxdbManager manager;
    //这里异步处理消息 最多只能同时多少批次的消息
    private ExecutorService executorService;

    public AyncSystemLogMessageHandle(InfluxdbManager manager,int threadPoolSize) {
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
                long t1 = System.currentTimeMillis();
                List<InfluxdbPo> pos = new ArrayList<>();
                messages.forEach(msg->{
                    try {
                        logger.info("接收系统消息msg={},开始处理",msg);
                        InfluxdbPo po = conver(msg);
                        if(po != null){
                            pos.add(po);
                        }else{
                            logger.warn("接收系统消息msg={},开始失败",msg);
                        }
                    }catch (Exception e){
                        logger.error("解析单条系统日志 msg={},失败,errorMsg={}",msg,e.getMessage());
                    }
                });
                if(!pos.isEmpty()){
                    manager.insertPoint(pos);
                }
                long t2 = System.currentTimeMillis();
                logger.info("批量处理{}条message耗时={}.ms",messages.size(),(t2-t1));
            }catch (Exception e){
                logger.error("系统日志msg={},数据处理失败,errorMsg={}",messages,e.getMessage());
            }
        });
        return true;
    }

    /**
     * 消息解析
     * @param message
     * @return
     */
    private InfluxdbPo conver(String message){
        InfluxdbPo po = null;
        JSONObject jsonObject = JSONObject.parseObject(message);
        JSONObject metricsetObj = jsonObject.getJSONObject("metricset");
        if(metricsetObj != null){
            Date createTime = jsonObject.getDate("@timestamp");
            if(createTime == null){
                createTime = new Date();
            }
            //模块
            String module = metricsetObj.getString("module");
            if(StringUtils.isBlank(module)){
                logger.info("message={} 没有module无法解析",message);
                return po;
            }
            //名称
            String name = metricsetObj.getString("name");
            if(StringUtils.isBlank(name)){
                logger.info("message={} 没有name无法解析",message);
                return po;
            }
            JSONObject moduleObj = jsonObject.getJSONObject(module);
            if(moduleObj != null){
                JSONObject collectionObj = moduleObj.getJSONObject(name);
                if(collectionObj == null){
                    logger.info("message={} 无数据收集无法解析",message);
                    return po;
                }
                po = new InfluxdbPo();
                putFields(collectionObj,po,StringUtils.EMPTY);
                if(po.getFields().isEmpty()){
                    logger.info("message={} 无数据收集无法解析",message);
                    return po;
                }
                po.setMeasurement(name);
                po.setCreateTime(createTime.getTime());
                JSONObject beatObj = jsonObject.getJSONObject("beat");
                if(beatObj != null){
                    String hostName = beatObj.getString("hostname");
                    if(StringUtils.isNotBlank(hostName)){
                        po.getTags().put("host_name",hostName);
                    }
                }
                String server_host = metricsetObj.getString("host");
                if(StringUtils.isNotBlank(server_host)){
                    po.getTags().put("server_host",server_host);
                }
                return po;
            }else{
                logger.info("message={} 无数据收集无法解析",message);
            }
        }
        return po;
    }

    /**
     * 解析属性
     * @param jsonObject
     * @param po
     * @param prefix
     */
    private void putFields(JSONObject jsonObject,InfluxdbPo po,String prefix){
        if(jsonObject != null){
            Map<String, Object> map = jsonObject.getInnerMap();
            for(Iterator<String> it = map.keySet().iterator();it.hasNext();){
                String key = it.next();
                Object value = map.get(key);
                String newKey = prefix.concat(key);
                if(value instanceof JSONObject){
                    putFields((JSONObject)value,po,newKey.concat("_"));
                }else{
                    Map<String,Object> fields = po.getFields();
                    if(fields == null){
                        fields = new HashMap<>();
                        po.setFields(fields);
                    }
                    Map<String,String> tags = po.getTags();
                    if(tags == null){
                        tags = new HashMap<>();
                        po.setTags(tags);
                    }
                    //查看是否是TAG
                    boolean isTag = false;
                    if(newKey.endsWith(TAG)){
                        newKey = newKey.substring(0,newKey.length() - TAG.length());
                        isTag = true;
                    }else if(newKey.endsWith(FIELD)){
                        newKey = newKey.substring(0,newKey.length() - FIELD.length());
                    }
                    //对类型进行判断
                    if(newKey.endsWith(type_float)){
                        try {
                            newKey = newKey.substring(0,newKey.length() - type_float.length());
                            if(isTag){
                                tags.put(newKey,value.toString());
                            }else{
                                fields.put(newKey,Float.parseFloat(value.toString()));
                            }
                        }catch (Exception e){
                            logger.info("conver float error,key={},value{},errormsg={}",newKey,value,e.getMessage());
                        }
                    }else if(newKey.endsWith(type_long)){
                        try {
                            newKey = newKey.substring(0,newKey.length() - type_long.length());
                            if(isTag){
                                tags.put(newKey,value.toString());
                            }else{
                                fields.put(newKey,Long.parseLong(value.toString()));
                            }
                        }catch (Exception e){
                            logger.info("conver long error,key={},value{},errormsg={}",newKey,value,e.getMessage());
                        }
                    }else if(newKey.endsWith(type_double)){
                        try {
                            newKey = newKey.substring(0,newKey.length() - type_double.length());
                            if(isTag){
                                tags.put(newKey,value.toString());
                            }else{
                                fields.put(newKey,Double.parseDouble(value.toString()));
                            }
                        }catch (Exception e){
                            logger.info("conver float error,key={},value{},errormsg={}",newKey,value,e.getMessage());
                        }
                    }else if(newKey.endsWith(type_string)){
                        try {
                            newKey = newKey.substring(0,newKey.length() - type_string.length());
                            if(isTag){
                                tags.put(newKey,value.toString());
                            }else{
                                fields.put(newKey,String.valueOf(value));
                            }
                        }catch (Exception e){
                            logger.info("conver String error,key={},value{},errormsg={}",newKey,value,e.getMessage());
                        }
                    }else {
                        if(isTag){
                            tags.put(newKey,value.toString());
                        }else{
                            fields.put(newKey,value);
                        }
                    }
                }
            }
        }
    }
}
