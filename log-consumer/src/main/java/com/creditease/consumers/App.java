package com.creditease.consumers;


import com.google.common.eventbus.AsyncEventBus;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);


    public static void main(String[] args) throws MQClientException {

        logger.info("start consumer .................");

        final AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(20));

        eventBus.register(new MessageProcessor());

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("ConsumerTest");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(5);
        //wrong time format 2017_0422_221800
        consumer.setConsumeMessageBatchMaxSize(1000);
        consumer.setNamesrvAddr("10.100.139.149:9876;10.100.139.150:9876");
        consumer.subscribe("TopicTest", "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

               System.out.printf(Thread.currentThread().getName() + " Receive New Messages: " + msgs.size() + "%n");
               msgs.forEach(msg ->
                   eventBus.post(msg)
               );

               return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
