package com.creditease.consumers;

import com.google.common.eventbus.Subscribe;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by creditease on 17/11/30.
 */
public class MessageProcessor {

    private  Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    @Subscribe
    public void processMessage(MessageExt msg){
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        Map<String,Object> data = new HashMap<String,Object>();
        data.put("host",msg.getBornHostNameString());
        data.put("msgid",msg.getMsgId());
        data.put("data",new String(msg.getBody()));
        data.put("born",msg.getBornTimestamp());
        dataList.add(data);
        InfluxdbManager.getInstance().insertPoint(dataList,"m-test");
    }

}
