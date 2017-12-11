package com.creditease.consumers.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * rocketMq 配置管理
 */
public class ApplicationProperties {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
    private static Properties prop = null;
    static {
        try {
            prop = new Properties();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(System.getProperty("source_location","application.properties"));
                if(in != null){
                prop.load(in);
            }else {
                logger.error("application.properties not exists");
                System.exit(1);
            }
        } catch (Throwable e) {
            logger.error("init application.properties fail",e);
            System.exit(1);
        }
    }

    public static String getRocketMqBrokerList() {
        return prop.getProperty("rocketmq.broker.list");
    }
    public static int getRocketMqDefualtConsumeThreadMin() {
        return Integer.parseInt(prop.getProperty("rocketmq.defualtConsumeThreadMin","10"));
    }
    public static int getRocketMqDefualtConsumeThreadMax() {
        return Integer.parseInt(prop.getProperty("rocketmq.defualtConsumeThreadMax","20"));
    }
    public static int getRocketMqDefualtConsumeMessageBatchMaxSize() {
        return Integer.parseInt(prop.getProperty("rocketmq.defualtConsumeMessageBatchMaxSize","1000"));
    }

    public static int getRocketMqDefaultConsumeConcurrentlyMaxSpan(){
        return Integer.parseInt(prop.getProperty("rocketmq.defaultConsumeConcurrentlyMaxSpan","2000"));
    }

    public static String getBizlogGroupName(){
        return prop.getProperty("rocketmq.bizlog.groupName");
    }
    public static String getBizlogTopic(){
        return prop.getProperty("rocketmq.bizlog.topic");
    }
    public static String getBizlogSubExpression(){
        return prop.getProperty("rocketmq.bizlog.subExpression");
    }


    //ETCD属性
    //连接超时时间(毫秒)
    public static int getEtcdConnectTimeout(){
        return Integer.parseInt(prop.getProperty("etcd.connectTimeout","3000"));
    }
    //数据传输长度(字节)
    public static int getEtcdMaxFrameSize(){
        return Integer.parseInt(prop.getProperty("etcd.maxFrameSize","102400"));
    }
    //#ETCD地址
    public static String getEtcdAddresses(){
        return prop.getProperty("etcd.addresses", StringUtils.EMPTY);
    }


    //Influxdb属性
    //Influxdb数据库地址
    public static String getInfluxDbAddress(){
        return prop.getProperty("influxdb.address");
    }
    //InfluxDB名称
    public static String getInfluxDBName(){
        return prop.getProperty("influxdb.database");
    }
}
