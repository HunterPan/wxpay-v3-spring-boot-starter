package com.jcidtech.pay.properties;

public class ScheduleJobProperties {
    //订单支付超时时间
    private Long expireTime;
    //订单巡回时间
    private Long intervalTime;

    public Long getExpireTime() {
        return expireTime;
    }
    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Long getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Long intervalTime) {
        this.intervalTime = intervalTime;
    }
}
