package com.jcidtech.pay.paypal.dto;

import com.jcidtech.pay.paypal.enums.PayPalNotifyType;
import lombok.Data;

@Data
public class BaseNotify {
    private PayPalNotifyType notifyType;
    private String outTradeNo;
    private Object content;
}
