package com.jcidtech.pay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jcidech.mp.base.BaseEntity;
import lombok.Data;

@Data
@TableName("tb_order")
public class Order extends BaseEntity {
    private String payTime;
    private Integer totalAmount;
    private Integer discountAmount;
    private Integer payAmount;
    private String payer;
    private String remark;
    private String outTradeNo;
    private String day;
    private String payChannel;
}
