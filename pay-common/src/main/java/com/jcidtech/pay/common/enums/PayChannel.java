package com.jcidtech.pay.common.enums;

import java.util.Objects;

public enum PayChannel {
    WX("wx","wxPay","微信"),
    ALI("ali","aliPay","支付宝"),
    PAYPAL("paypal","payPalPay","paypal"),
    UNKNOWN("unknown","unknown","未知"),
    ;
    private String channel;
    private String beanName;
    private String remark;

    PayChannel(String channel, String beanName,String remark) {
        this.channel = channel;
        this.beanName = beanName;
        this.remark = remark;
    }


    public String getChannel() {
        return channel;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getRemark() {
        return remark;
    }

    public static PayChannel getByChannel(String channel){
        for(PayChannel payChannel:PayChannel.values()){
            if(Objects.equals(payChannel.channel,channel)){
                return payChannel;
            }
        }
        return UNKNOWN;
    }
}
