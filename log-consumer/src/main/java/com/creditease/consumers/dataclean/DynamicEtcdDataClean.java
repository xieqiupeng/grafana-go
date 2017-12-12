package com.creditease.consumers.dataclean;

import com.alibaba.fastjson.JSON;
import mousio.client.promises.ResponsePromise;
import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 数据业务业务清洗
 */
public class DynamicEtcdDataClean implements IDataClean {

    private static final Logger logger = LoggerFactory.getLogger(DynamicEtcdDataClean.class);

    private EtcdClient etcdClient;

    private String watchPath;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ConcurrentHashMap<String, IDataCleanRule> cleanRuleMap = new ConcurrentHashMap<>();

    public DynamicEtcdDataClean(EtcdClient etcdClient, String watchPath) {
        this.etcdClient = etcdClient;
        this.watchPath = watchPath;
        init();
    }

    public void init() {
        //启动ETCD抓取数据
        try {
            //初始化只需用尝试连接3次
            EtcdResponsePromise<EtcdKeysResponse> promise = etcdClient.getDir(watchPath).recursive().send();
            EtcdKeysResponse response = promise.get();
            //启动ETCD监听
            executorService.execute(() -> { watch(response.etcdIndex + 1); });
            //获取数据
            refreshCleanRule(response.getNode().getNodes());
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
        logger.info("注册监听开始");
        for (;;) {
            try {
                EtcdResponsePromise<EtcdKeysResponse> promise = etcdClient.getDir(watchPath).recursive()
                        .waitForChange(waitIndex).timeout(-1, TimeUnit.SECONDS)
                        .setRetryPolicy(RetryWithExponentialBackOff.DEFAULT).send();
                promise.addListener(new ResponsePromise.IsSimplePromiseResponseHandler<EtcdKeysResponse>() {
                    @Override
                    public void onResponse(ResponsePromise<EtcdKeysResponse> response) {
                        long index = waitIndex;
                        try {
                            Throwable es = response.getException();
                            if (es != null) {
                                logger.error("watch error", es);
                                logger.info("全量数据重新更新开始");
                                for (;;) {
                                    try {
                                        EtcdResponsePromise<EtcdKeysResponse> promise = etcdClient.getDir(watchPath).recursive()
                                                .setRetryPolicy(RetryWithExponentialBackOff.DEFAULT).send();
                                        EtcdKeysResponse totalResponse = promise.get();
                                        refreshCleanRule(totalResponse.getNode().getNodes());
                                        index = totalResponse.etcdIndex + 1;
                                        break;
                                    } catch (Exception e) {
                                        logger.error("全量数据更新异常", e);
                                        try {
                                            Thread.sleep(2000);
                                        }catch (Exception ex){}
                                        logger.error("全量数据重新更新重试开始", e);
                                    }
                                }
                                logger.info("全量数据重新更新结束");
                            } else {
                                logger.info("增量数据更新开始");
                                EtcdKeysResponse keysResponse = response.getNow();
                                if (keysResponse != null) {
                                    refreshCleanRule(keysResponse.getNode());
                                }
                                logger.info("增量数据更新结束");
                                ++index;
                            }
                        } catch (Exception e) {
                            logger.error("IsSimplePromiseResponseHandler", e);
                        } finally {
                            //关闭Response
                            response.cancel();
                            //移出Listener，让内存可回收
                            promise.removeListener(this);
                            //重新注册监听
                            watch(index);
                        }
                    }
                });
                logger.info("注册监听结束");
                break;
            } catch (Exception e) {
                logger.error("waitForChange watch error", e);
                try {
                    Thread.sleep(2000);
                } catch (Exception ex) {}
                logger.info("重新尝试注册监听开始");
            }
        }
    }

    private void refreshCleanRule(List<EtcdKeysResponse.EtcdNode> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            nodes.forEach(note -> {refreshCleanRule(note);});
        }
    }

    //刷新规则
    private void refreshCleanRule(EtcdKeysResponse.EtcdNode node) {
        try {
            String nodeDataJson = JSON.toJSONString(node);
            logger.info("refreshCleanRule start node={}", nodeDataJson);
            String dateSoureName = node.getKey().replace(watchPath, StringUtils.EMPTY).replace("/", StringUtils.EMPTY);
            String nodeValue = node.getValue();
            if (StringUtils.isBlank(nodeValue)) {
                cleanRuleMap.remove(dateSoureName);
                logger.info("删除dateSoureName={}成功", dateSoureName);
            } else {
                MonitorNoteDataEntity noteData = JSON.parseObject(nodeValue,MonitorNoteDataEntity.class);
                String cleanRuleStr = noteData.getCleanRule();
                if(StringUtils.isNotBlank(cleanRuleStr)){
                    DataCleanRuleEntity dataCleanEntity = JSON.parseObject(cleanRuleStr,DataCleanRuleEntity.class);
                    IDataCleanRule dataCleanRule = DataCleanUtil.getDataCleanRule(dataCleanEntity);
                    if(dataCleanRule != null){
                        cleanRuleMap.put(dateSoureName,dataCleanRule);
                    }
                }
                logger.info("添加dateSoureName={}成功", dateSoureName);
            }
            logger.info("refreshCleanRule end node={}", nodeDataJson);
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
}
