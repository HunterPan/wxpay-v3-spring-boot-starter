package com.jcidtech.pay.common.error;

public class PayException extends BaseException{
    public PayException() {
    }

    public PayException(String message, int code) {
        super(message, code);
    }
}
