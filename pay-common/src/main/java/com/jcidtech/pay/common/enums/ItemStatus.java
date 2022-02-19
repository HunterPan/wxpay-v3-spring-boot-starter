package com.jcidtech.pay.common.enums;

import java.util.Objects;

public enum ItemStatus {
    ON(1,"上架"),
    OFF(2,"下架"),
    UNKNOWN(3,"未知"),
    ;
    private int value;
    private String remark;

    ItemStatus(int value, String remark) {
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

    public static ItemStatus getByStatus(int status){
        for(ItemStatus payChannel:ItemStatus.values()){
            if(payChannel.value == status){
                return payChannel;
            }
        }
        return ON;
    }
}
