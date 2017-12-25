package com.creditease.consumers.util;

import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
                EtcdNettyClient client = null;
                String[] addressArray = ApplicationProperties.getEtcdAddresses().split(",");
                List<URI> list = new ArrayList<>();
                for (String address : addressArray) {
                    list.add(new URI(protocol.concat(address)));
                }
                client = new EtcdNettyClient(nettyConfig, list.toArray(new URI[list.size()]));
                etcdClient = new EtcdClient(client);
                etcdClient.setRetryHandler(new RetryWithExponentialBackOff(1000, 3, 5000));
            } catch (Throwable e) {
                logger.error("Setting up etcdClient fail!", e);
                System.exit(1);
            }
        }
        logger.info("Done.");
    }

    public static EtcdClient getEtcdClient() {
        return etcdClient;
    }
}
