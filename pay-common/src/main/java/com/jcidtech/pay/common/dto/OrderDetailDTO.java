package com.jcidtech.pay.common.dto;

import lombok.Data;

@Data
public class OrderDetailDTO extends BaseDTO{
    private String payChannel;
    private Long itemId;
    private Long discountAmt;
    private Long userId;
}
