package com.creditease.monitor.etcd.config;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.exception.EtcdException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Configuration
public class EtcdConfig {
    private static final String PROTOCOL = "http://";
    @Value("${etcd.connectTimeout}")
    private int connectTimeout;
    @Value("${etcd.maxFrameSize}")
    private int maxFrameSize;
    @Value("${etcd.addresses}")
    private String addresses;

    @Bean
    public Client initEtcdClient() {
        String[] addressArray = addresses.split(",");
        for (int i = 0; i < addressArray.length; i++) {
            addressArray[i] = PROTOCOL.concat(addressArray[i]);
        }
        Client client = Client.builder()
                .endpoints(addressArray)
                .build();
        return client;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        String[] endpoints = {
                "http://10.100.139.153:2379",
                "http://10.100.139.151:2379",
                "http://10.100.139.150:2379",
        };
        //
        Client client = Client.builder()
                .endpoints(endpoints)
                .build();
        try {
            client.getKVClient()
                    .put(ByteSequence.fromString("/test/test8"),
                            ByteSequence.fromString("8"))
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
