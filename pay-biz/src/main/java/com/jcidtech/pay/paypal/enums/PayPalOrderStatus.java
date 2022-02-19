package com.jcidtech.pay.paypal.enums;

// see https://developer.paypal.com/api/orders/v2/#orders_capture
public enum PayPalOrderStatus {
    CREATED("CREATED","创建"),
    SAVED("SAVED","保存"),
    APPROVED("APPROVED","用户同意支付"),
    VOIDED("VOIDED","空没有购物"),
    COMPLETED("COMPLETED","完成"),
    PAYER_ACTION_REQUIRED("PAYER_ACTION_REQUIRED","等待用户操作"),
    CAPTURE("CAPTURE","等待用户checkOut"),
    ;
    private String status;
    private String remark;

    PayPalOrderStatus(String status, String remark) {
        this.status = status;
        this.remark = remark;
    }

    public String getStatus() {
        return status;
    }

    public String getRemark() {
        return remark;
    }
}
