package com.creditease.consumers;


import com.creditease.consumers.dataclean.DynamicEtcdDataClean;
import com.creditease.consumers.dataclean.IDataClean;
import com.creditease.consumers.influxdb.InfluxdbManager;
import com.creditease.consumers.message.AyncBizLogMessageHandle;
import com.creditease.consumers.message.MessageHandler;
import com.creditease.consumers.util.ApplicationProperties;
import com.creditease.consumers.util.EtcdClientUtil;
import com.creditease.consumers.util.RocketMqUtil;
import mousio.etcd4j.EtcdClient;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App {

    public static void main(String[] args) throws Exception {
        if(args != null && args.length > 0){
            //设置log日志位置为指定路径
            System.setProperty("log.home",args[0]);
            //设置读取的资源文件
            if(args.length > 1){
                System.setProperty("source_location",args[1]);
            }
        }else{
            //设置log位置为当前目录
            System.setProperty("log.home",".");
        }
        Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("start consumer .................");
        setUp();
        logger.info("start consumer success.................");
    }

    private static void setUp() throws MQClientException {
        EtcdClient etcdClient = EtcdClientUtil.getEtcdClient();
        IDataClean dataClean = new DynamicEtcdDataClean(etcdClient,"/monitor");
        InfluxdbManager manager = new InfluxdbManager(ApplicationProperties.getInfluxDbAddress(),ApplicationProperties.getInfluxDBName());
        MessageHandler bizLogMessageHandler = new AyncBizLogMessageHandle(manager,dataClean,10);
        RocketMqUtil.startConsumer(ApplicationProperties.getBizlogGroupName(),ApplicationProperties.getBizlogTopic(),ApplicationProperties.getBizlogSubExpression(),bizLogMessageHandler);
    }
}
