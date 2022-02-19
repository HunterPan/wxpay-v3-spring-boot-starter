package com.jcidtech.pay.common.enums;

import java.util.Objects;

public enum OrderStatus {
    INIT(1,"未支付"),
    PAID(2,"已支付"),
    UN_PAID_FINISH(3,"订单已关闭，未支付"),
    TIMEOUT_CLOSED(4,"超时关闭，主动"),
    UNKNOWN(5,"异常状态，请检查"),
    ;
    private int value;
    private String remark;

    OrderStatus(int value, String remark) {
        this.value = value;
        this.remark = remark;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public static OrderStatus getByStatus(Integer status){
        for(OrderStatus payChannel:OrderStatus.values()){
            if(Objects.equals(payChannel.value,status)){
                return payChannel;
            }
        }
        return PAID;
    }
}
