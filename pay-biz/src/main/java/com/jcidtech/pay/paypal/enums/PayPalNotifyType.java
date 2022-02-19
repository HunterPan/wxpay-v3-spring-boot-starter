package com.jcidtech.pay.paypal.enums;

public enum PayPalNotifyType {
    PAYMENT_CAPTURE_COMPLETED("PAYMENT.CAPTURE.COMPLETED","扣划金额完成"),
    ORDER_APPROVED("ORDER_APPROVED","用户确认支付"),
    ORDER_CANCEL("ORDER_CANCEL","用户取消支付"),
    ;


    private final String type;

    private final String remark;

    PayPalNotifyType(String type, String remark) {
        this.type = type;
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }
}
