package com.jcidtech.pay.common.enums;

public enum PayStatus {
    SUCCESS(1,"已支付"),
    ORDER_CLOSE(2,"订单已关闭，未支付"),
    OTHER(3,"其他情况"),
    ;
    private int code;
    private String desc;

    PayStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
