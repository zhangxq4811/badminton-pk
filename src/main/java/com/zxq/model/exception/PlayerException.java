package com.zxq.model.exception;

/**
 * 自定义异常
 * @author zhangxianqing
 */
public class PlayerException extends RuntimeException {

    private String message;

    public PlayerException() {
        super();
    }

    public PlayerException(String msg) {
        super(msg);
        this.message = msg;
    }

}
