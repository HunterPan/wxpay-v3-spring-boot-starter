package com.jcidtech.pay.common.error;

public class BaseException extends RuntimeException{
    private int code;

    public BaseException() {
        super(ErrorCode.FAILURE.getMsg());
        this.code = ErrorCode.FAILURE.getCode();
    }

    public BaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }

}
