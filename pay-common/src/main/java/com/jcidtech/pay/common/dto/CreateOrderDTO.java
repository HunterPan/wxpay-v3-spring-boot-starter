package com.jcidtech.pay.common.dto;

import lombok.Data;

@Data
public class CreateOrderDTO extends BaseDTO{
    private Long itemId;
    private String payChannel;
}
