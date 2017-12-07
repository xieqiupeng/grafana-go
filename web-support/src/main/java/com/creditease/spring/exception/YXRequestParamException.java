package com.creditease.spring.exception;

public class YXRequestParamException extends RuntimeException{

    private String disMessage;

    public YXRequestParamException() {
        super();
    }

    public YXRequestParamException(String message) {
        super(message);
    }

    public YXRequestParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public YXRequestParamException(String message,String displayMessage) {
        super(message);
        this.disMessage = displayMessage;
    }

    public YXRequestParamException(String message, Throwable cause,String displayMessage) {
        super(message, cause);
        this.disMessage = displayMessage;
    }

    public YXRequestParamException(Throwable cause) {
        super(cause);
    }

    public String getDisMessage() {
        return disMessage;
    }
}
