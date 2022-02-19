package com.jcidtech.pay.paypal.dto;

import lombok.Data;

@Data
public class PaySuccessNotify {
    //token
    private String token;
    private String paymentId;
    private String payerId;

    @Override
    public String toString() {
        return "PaySuccessInfo{" +
                "token='" + token + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", payerId='" + payerId + '\'' +
                '}';
    }
}
