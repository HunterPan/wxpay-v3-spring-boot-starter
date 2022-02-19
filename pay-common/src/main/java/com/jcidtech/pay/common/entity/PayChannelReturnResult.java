package com.jcidtech.pay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_channel_pay_return")
public class PayChannelReturnResult implements Serializable {
    private Long id;
    private Long payId;
    private String outTradeNo;
    private String channelDetail;
    private Date createTime;
    private Date updateTime;
}
