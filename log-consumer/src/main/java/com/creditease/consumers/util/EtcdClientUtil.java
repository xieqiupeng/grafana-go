package com.creditease.consumers.util;

import com.alibaba.fastjson.JSON;
import com.creditease.consumers.dataclean.DataCleanRuleEntity;
import com.creditease.consumers.dataclean.DynamicEtcdDataClean;
import com.creditease.consumers.dataclean.MonitorNoteDataEntity;
import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class EtcdClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(EtcdClientUtil.class);
    private static final String protocol = "http://";
    private static EtcdClient etcdClient;
    static {
        setup();
    }

    /**
     * @Description:建立ETCD连接
     */
    public synchronized static void setup() {
        logger.info("Setting up etcdClient.");
        if (etcdClient == null) {
            try {
                EtcdNettyConfig nettyConfig = new EtcdNettyConfig();
                nettyConfig.setConnectTimeout(ApplicationProperties.getEtcdConnectTimeout());
                nettyConfig.setMaxFrameSize(ApplicationProperties.getEtcdMaxFrameSize());
                EtcdNettyClient client= null;
                String[] addressArray = ApplicationProperties.getEtcdAddresses().split(",");
                List<URI> list = new ArrayList<>();
                for(String address : addressArray){
                    list.add(new URI(protocol.concat(address)));
                }
                client = new EtcdNettyClient(nettyConfig,list.toArray(new URI[list.size()]));
                etcdClient = new EtcdClient(client);
                etcdClient.setRetryHandler(new RetryWithExponentialBackOff(1000,3,5000));
            } catch (Throwable e) {
                logger.error("Setting up etcdClient fail!", e);
                System.exit(1);
            }
        }
        logger.info("Done.");
    }

    public static EtcdClient getEtcdClient(){
        return etcdClient;
    }

    public static void main(String[] args) throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException {
        DataCleanRuleEntity entity = new DataCleanRuleEntity();
        entity.setTemplate("0");
        DataCleanRuleEntity.Separator separator = new DataCleanRuleEntity.Separator();
        separator.setOrder(false);
        separator.setRegex(false);
        List<String> separatorKeys = new ArrayList<>();
        separatorKeys.add("|");
        separatorKeys.add(",");
        separator.setSeparatorKeys(separatorKeys);
        entity.setSeparator(separator);
        List<DataCleanRuleEntity.DataMapping> list = new ArrayList<>();
        DataCleanRuleEntity.DataMapping mapping = new DataCleanRuleEntity.DataMapping();
        mapping.setColumnSeq(0);
        mapping.setColumnName("region");
        mapping.setColumnType("string");
        mapping.setFormat("");
        mapping.setTagOrValue(0);
        list.add(mapping);
        mapping = new DataCleanRuleEntity.DataMapping();
        mapping.setColumnSeq(1);
        mapping.setColumnName("count");
        mapping.setColumnType("long");
        mapping.setFormat("");
        mapping.setTagOrValue(1);
        list.add(mapping);
        mapping = new DataCleanRuleEntity.DataMapping();
        mapping.setColumnSeq(2);
        mapping.setColumnName("price");
        mapping.setColumnType("double");
        mapping.setFormat("");
        mapping.setTagOrValue(1);
        list.add(mapping);
        mapping = new DataCleanRuleEntity.DataMapping();
        mapping.setColumnSeq(3);
        mapping.setColumnName("orderDate");
        mapping.setColumnType("date");
        mapping.setFormat("yyyy-MM-dd HH:mm:ss");
        mapping.setTagOrValue(1);
        list.add(mapping);
        entity.setResultColumns(list);
        //System.out.println(JSON.toJSONString(entity));

        MonitorNoteDataEntity noteData = new MonitorNoteDataEntity();
        noteData.setCleanRule(JSON.toJSONString(entity));
        noteData.setType("log");
        noteData.setPath("/app/testLog.log");
        List<String> ips = new ArrayList<>();
        ips.add("10.100.139.150");
        ips.add("10.100.139.151");
        noteData.setIpAddress(ips);

        getEtcdClient().put("/monitor/gfwTest",JSON.toJSONString(noteData)).send();
        //System.out.println(JSON.toJSONString(getEtcdClient().get("/monitor/gfwTest").send().get()));
    }
}
