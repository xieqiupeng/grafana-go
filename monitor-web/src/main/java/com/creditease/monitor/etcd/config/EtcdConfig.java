package com.creditease.monitor.etcd.config;

import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
public class EtcdConfig {
    private static final String protocol = "http://";
    @Value("${etcd.connectTimeout}")
    private int connectTimeout;
    @Value("${etcd.maxFrameSize}")
    private int maxFrameSize;
    @Value("${etcd.addresses}")
    private String addresses;

    @Bean
    public EtcdClient initEtcdClient() throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException {
        EtcdNettyConfig nettyConfig = new EtcdNettyConfig();
        nettyConfig.setConnectTimeout(connectTimeout);
        nettyConfig.setMaxFrameSize(maxFrameSize);
        EtcdNettyClient client= null;
        try {
            String[] addressArray = addresses.split(",");
            List<URI> list = new ArrayList<>();
            for(String address : addressArray){
                list.add(new URI(protocol.concat(address)));
            }
            client = new EtcdNettyClient(nettyConfig,list.toArray(new URI[list.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        EtcdClient etcdClient = new EtcdClient(client);
        etcdClient.setRetryHandler(new RetryWithExponentialBackOff(1000,3,5000));
        return etcdClient;
    }
}
