package com.jcidtech.pay.properties;

public class JcProperties {
    //域名
    private String host;
    private ScheduleJobProperties job;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ScheduleJobProperties getJob() {
        return job;
    }

    public void setJob(ScheduleJobProperties job) {
        this.job = job;
    }
}
