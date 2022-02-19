package com.jcidtech.pay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_pay_schedule_job")
public class PayScheduleJob implements Serializable {
    private Long id;
    private Long payId;
    private String payChannel;
    private Date nextTime;
    private Date expireTime;
    private Date createTime;
}
