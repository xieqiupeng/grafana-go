package com.creditease.consumers.message;

import java.util.List;

/**
 * 消息处理接口抽象
 */
public interface MessageHandler {
    /**
     *
     * @param messages 消息体
     */
    public boolean handleMessage(List<String> messages);
}
