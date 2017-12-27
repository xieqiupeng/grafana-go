package com.creditease.consumers.dataclean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchEvent;
import com.coreos.jetcd.watch.WatchResponse;
import com.creditease.consumers.util.EtcdClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据业务业务清洗
 */
public class DynamicEtcdDataClean implements IDataClean ,Closeable{

    private static final Logger logger = LoggerFactory.getLogger(DynamicEtcdDataClean.class);

    private String watchPath;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ConcurrentHashMap<String, IDataCleanRule> cleanRuleMap = new ConcurrentHashMap<>();

    private Client client;

    public DynamicEtcdDataClean(Client client, String watchPath) {
        this.client = client;
        this.watchPath = watchPath;
        init();
    }

    public void init() {
        //启动ETCD抓取数据
        try {
            KV kvClient = client.getKVClient();
            CompletableFuture<GetResponse> completableFuture =  kvClient.get(ByteSequence.fromString(watchPath),
                    GetOption.newBuilder().withPrefix(ByteSequence.fromString(watchPath)).build());
            GetResponse response =  completableFuture.get();
            List<KeyValue> keyValues = response.getKvs();
            long watchIndex = -1;
            for(KeyValue keyValue :keyValues)  {
                refreshCleanRule(keyValue.getKey().toStringUtf8(),keyValue.getValue().toStringUtf8());
                watchIndex = keyValue.getModRevision();
            }
            long waitIndex = watchIndex + 1;
            kvClient.close();
            //注册监听
            executorService.execute(()->{watch(waitIndex);});
            //初始化只需用尝试连接3次
            //EtcdResponsePromise<EtcdKeysResponse> promise = etcdClient.getDir(watchPath).recursive().send();
           // EtcdKeysResponse response = promise.get();
            //启动ETCD监听
           // executorService.execute(() -> { watch(response.etcdIndex + 1); });
            //获取数据
            //refreshCleanRule(response.getNode().getNodes());
        } catch (Exception e) {
            logger.error("etcd init error", e);
            System.exit(1);
        }
    }

    /**
     * 不中断的监听,如果出现异常,或者链接中断期间有过多的变更,会进行全量更新
     * @param waitIndex
     */
    private void watch(long waitIndex) {
        Watch watchClient = client.getWatchClient();
        for (;;){
            try {
                logger.info("注册监听开始 waitIndex={}",waitIndex);
                WatchOption watchOption = WatchOption.newBuilder().withPrefix(ByteSequence.fromString(watchPath)).withRevision(waitIndex).build();
                Watch.Watcher watch = watchClient.watch(ByteSequence.fromString(watchPath),watchOption);
                WatchResponse watchResponse = watch.listen();
                List<WatchEvent> events =  watchResponse.getEvents();
                for(WatchEvent event : events){
                    String key = event.getKeyValue().getKey().toStringUtf8();
                    String value = event.getKeyValue().getValue().toStringUtf8();
                    logger.info("接收ETCD切割数据刷新规则开始:key={},value={}",key,value);
                    refreshCleanRule(key,value);
                    waitIndex = event.getKeyValue().getModRevision() + 1;
                    logger.info("接收ETCD切割数据刷新规则结束:key={},value={}",key,value);
                }
                logger.info("注册监听结束");
            }catch (Exception e){
                logger.error("watch etcd error",e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                logger.info("watch restart");
            }
        }
//        for (;;) {
//            try {
//                EtcdResponsePromise<EtcdKeysResponse> promise = etcdClient.getDir(watchPath).recursive()
//                        .waitForChange(waitIndex).timeout(-1, TimeUnit.SECONDS)
//                        .setRetryPolicy(RetryWithExponentialBackOff.DEFAULT).send();
//                promise.addListener(new ResponsePromise.IsSimplePromiseResponseHandler<EtcdKeysResponse>() {
//                    @Override
//                    public void onResponse(ResponsePromise<EtcdKeysResponse> response) {
//                        long index = waitIndex;
//                        try {
//                            Throwable es = response.getException();
//                            if (es != null) {
//                                //如果是The event in requested index is outdated and cleared 则进行全量更新
//                                if(es instanceof EtcdException && ((EtcdException) es).getErrorCode() == EtcdErrorCode.EventIndexCleared){
//                                    logger.error("watch error", es);
//                                    logger.info("全量数据重新更新开始");
//                                    for (;;) {
//                                        try {
//                                            EtcdResponsePromise<EtcdKeysResponse> promise = etcdClient.getDir(watchPath).recursive()
//                                                    .setRetryPolicy(RetryWithExponentialBackOff.DEFAULT).send();
//                                            EtcdKeysResponse totalResponse = promise.get();
//                                            refreshCleanRule(totalResponse.getNode().getNodes());
//                                            index = totalResponse.etcdIndex + 1;
//                                            break;
//                                        } catch (Exception e) {
//                                            logger.error("全量数据更新异常", e);
//                                            try {
//                                                Thread.sleep(2000);
//                                            }catch (Exception ex){}
//                                            logger.error("全量数据重新更新重试开始", e);
//                                        }
//                                    }
//                                    logger.info("全量数据重新更新结束");
//                                }
//                            } else {
//                                logger.info("增量数据更新开始");
//                                EtcdKeysResponse keysResponse = response.getNow();
//                                if (keysResponse != null) {
//                                    refreshCleanRule(keysResponse.getNode());
//                                }
//                                logger.info("增量数据更新结束");
//                                ++index;
//                            }
//                        } catch (Exception e) {
//                            logger.error("IsSimplePromiseResponseHandler", e);
//                        } finally {
//                            //关闭Response
//                            response.cancel();
//                            //移出Listener，让内存可回收
//                            promise.removeListener(this);
//                            //重新注册监听
//                            watch(index);
//                        }
//                    }
//                });
//                logger.info("注册监听结束");
//                break;
//            } catch (Exception e) {
//                logger.error("waitForChange watch error", e);
//                try {
//                    Thread.sleep(2000);
//                } catch (Exception ex) {}
//                logger.info("重新尝试注册监听开始");
//            }
//        }
    }

//    private void refreshCleanRule(List<EtcdKeysResponse.EtcdNode> nodes) {
//        if (nodes != null && !nodes.isEmpty()) {
//            nodes.forEach(note -> {refreshCleanRule(note);});
//        }
//    }

    //刷新规则
    private void refreshCleanRule(String key,String value) {
        try {
            if(StringUtils.isBlank(key)){
                return;
            }
            logger.info("refreshCleanRule start key={},value={}", key,value);
            String dateSoureName = key.replace(watchPath, StringUtils.EMPTY).replace("/", StringUtils.EMPTY);
            if (StringUtils.isBlank(value)) {
                cleanRuleMap.remove(dateSoureName);
                logger.info("删除dateSoureName={}成功", dateSoureName);
            } else {
                JSONObject jsonObject = JSON.parseObject(value);
                String cleanRuleStr = jsonObject.getString("cleanRule");
                if(StringUtils.isNotBlank(cleanRuleStr)){
                    DataCleanRuleEntity dataCleanEntity = JSON.parseObject(cleanRuleStr,DataCleanRuleEntity.class);
                    IDataCleanRule dataCleanRule = DataCleanUtil.getDataCleanRule(dataCleanEntity);
                    if(dataCleanRule != null){
                        cleanRuleMap.put(dateSoureName,dataCleanRule);
                    }
                }
                logger.info("添加dateSoureName={}成功", dateSoureName);
            }
            logger.info("refreshCleanRule end key={},value={}", key,value);
        } catch (Exception e) {
            logger.error("refreshCleanRule error", e);
        }
    }

    @Override
    public List<DataCleanEntity> clean(String monitorName, String monitorData) {
        if(StringUtils.isNotBlank(monitorName)){
            IDataCleanRule dataCleanRule = cleanRuleMap.get(monitorName);
            if(dataCleanRule != null){
                return dataCleanRule.clean(monitorData);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void close() throws IOException {
        executorService.shutdownNow();
        client.close();
    }
}
