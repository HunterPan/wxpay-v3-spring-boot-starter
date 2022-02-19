package com.jcidtech.pay.common.vo;

import com.jcidtech.pay.common.dto.BaseDTO;
import lombok.Data;

import java.util.Date;

@Data
public class OrderDetailVO extends BaseDTO {
    private Long id;
    private String payTime;
    private Integer totalAmount;
    private Integer discountAmount;
    private Integer payAmount;
    private String remark;
    private String outTradeNo;
    private Long itemId;
    private String itemTitle;
    private Integer status;
    private String payer;
    private String statusName;
    private String payChannel;
    private String payChannelName;
    private Date createTime;
}
