package com.example.picture.exception;

import lombok.Getter;

/**
 * 自定义异常类，在运行时抛出异常
 */
@Getter
public class BussinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BussinessException(String message,int code,String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BussinessException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public BussinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
}