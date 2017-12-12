package com.creditease.consumers.util;

import com.creditease.consumers.message.MessageHandler;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.ArrayList;
import java.util.List;

/**
 * RocketMq 工具类
 */
public class RocketMqUtil {

    public static void startConsumer(String groupName, String topic, String subExpression,
                                     MessageHandler messageHandler) throws MQClientException {
        startConsumer(groupName, topic, subExpression,ApplicationProperties.getRocketMqDefaultConsumeConcurrentlyMaxSpan(),messageHandler);

    }

    public static void startConsumer(String groupName, String topic, String subExpression,
                                     int consumeConcurrentlyMaxSpan, MessageHandler messageHandler) throws MQClientException {
        startConsumer(groupName, topic, subExpression,consumeConcurrentlyMaxSpan, ApplicationProperties.getRocketMqDefualtConsumeThreadMin(),
                ApplicationProperties.getRocketMqDefualtConsumeThreadMax(),
                ApplicationProperties.getRocketMqDefualtConsumeMessageBatchMaxSize(),
                messageHandler);

    }
    /**
     * 启动RocketMqConsumer
     * @param groupName
     * @param topic
     * @param subExpression
     * @param consumeThreadMin
     * @param consumeThreadMax
     * @param consumeMessageBatchMaxSize
     * @param messageHandler
     * @throws MQClientException
     */
    public static void startConsumer(String groupName, String topic, String subExpression, int consumeConcurrentlyMaxSpan,int consumeThreadMin,
                                     int consumeThreadMax, int consumeMessageBatchMaxSize, MessageHandler messageHandler) throws MQClientException {
        MQPushConsumer consumer = buildRocketMqConsumer(groupName, topic, subExpression, consumeConcurrentlyMaxSpan, consumeThreadMin, consumeThreadMax, consumeMessageBatchMaxSize);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
               List<String> msgs = new ArrayList<>();
               list.forEach(msg->msgs.add(new String(msg.getBody())));
               boolean ok = messageHandler.handleMessage(msgs);
               if(ok){
                   return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
               }
               return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        consumer.start();
    }

    /**
     * 创建RocketMqConsumer
     * @param groupName
     * @param topic
     * @param subExpression
     * @param consumeThreadMin
     * @param consumeThreadMax
     * @param consumeMessageBatchMaxSize
     * @return
     * @throws MQClientException
     */
    private static MQPushConsumer buildRocketMqConsumer(String groupName, String topic, String subExpression,
                                                        int consumeConcurrentlyMaxSpan,int consumeThreadMin,
                                                        int consumeThreadMax, int consumeMessageBatchMaxSize) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
        consumer.setConsumeConcurrentlyMaxSpan(consumeConcurrentlyMaxSpan);
        consumer.setNamesrvAddr(ApplicationProperties.getRocketMqBrokerList());
        consumer.subscribe(topic, subExpression);
        return consumer;
    }
}
