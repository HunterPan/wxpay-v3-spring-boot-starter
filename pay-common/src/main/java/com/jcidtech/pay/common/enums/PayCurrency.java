package com.jcidtech.pay.common.enums;

public enum PayCurrency {
    CNY("CNY","人民币"),
    ;
    private String currency;
    private String remark;

    PayCurrency(String currency, String remark) {
        this.currency = currency;
        this.remark = remark;
    }

    public String getCurrency() {
        return currency;
    }
    public String getRemark() {
        return remark;
    }
}
