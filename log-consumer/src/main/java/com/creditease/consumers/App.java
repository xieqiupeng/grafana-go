package com.creditease.consumers;


import com.creditease.consumers.dataclean.DynamicEtcdDataClean;
import com.creditease.consumers.dataclean.IDataClean;
import com.creditease.consumers.influxdb.InfluxdbManager;
import com.creditease.consumers.message.AyncBizLogMessageHandle;
import com.creditease.consumers.message.AyncSystemLogMessageHandle;
import com.creditease.consumers.message.MessageHandler;
import com.creditease.consumers.util.ApplicationProperties;
import com.creditease.consumers.util.EtcdClientUtil;
import com.creditease.consumers.util.RocketMqUtil;
import mousio.etcd4j.EtcdClient;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        //业务日志收集处理开始
        EtcdClient etcdClient = EtcdClientUtil.getEtcdClient();
        IDataClean dataClean = new DynamicEtcdDataClean(etcdClient,"/monitor");
        InfluxdbManager manager = new InfluxdbManager(ApplicationProperties.getInfluxDbAddress(),ApplicationProperties.getInfluxDBName());
        MessageHandler bizLogMessageHandler = new AyncBizLogMessageHandle(manager,dataClean,10);
        RocketMqUtil.startConsumer(ApplicationProperties.getBizlogGroupName(),ApplicationProperties.getBizlogTopic(),ApplicationProperties.getBizlogSubExpression(),bizLogMessageHandler);

        //系统日志收集处理开始
        MessageHandler sysLogMessageHandler = new AyncSystemLogMessageHandle(manager,10);
        RocketMqUtil.startConsumer(ApplicationProperties.getSyslogGroupName(),ApplicationProperties.getSyslogTopic(),ApplicationProperties.getSyslogSubExpression(),sysLogMessageHandler);
    }
}
