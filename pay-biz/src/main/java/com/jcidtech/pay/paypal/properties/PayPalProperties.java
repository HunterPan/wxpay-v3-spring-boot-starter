package com.jcidtech.pay.paypal.properties;

import lombok.Data;

@Data
public class PayPalProperties {
    //sandbox live
    private String mode;
    private String clientId;
    private String secret;
    private String brandName;
    private String returnUrl;
    private String cancelUrl;
}
