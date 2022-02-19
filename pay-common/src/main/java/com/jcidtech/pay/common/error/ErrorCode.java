package com.jcidtech.pay.common.error;

public enum ErrorCode {
    SUCCESS(200, "success"),
    FAILURE(300, "failure"),
    UNCHECKED(301, "uncheck"),
    NO_AUTH(302, "no auth"),
    ITEM_VALID(400, "商品不存在或已下架"),
    ITEM_PAY_AMOUNT_VALID(401, "订单金额不正确，请联系管理员"),

    ORDER_CREATE_NATIVE_ERROR(500, "第三方生成订单错误,请稍后再试"),
    PAYPAL_CAPTURE_ORDER_ERROR(501, "payPal capture order error"),
    ILLEGAL_CONTENT(600, "内容格式有误"),
    ILLEGAL_URL_PARAM(601, "URL参数有误"),
    //wx
    WX_CERTIFICATE_INIT_ERROR(650, "微信证书错误"),
    WX_ENCRYPT_DATA_ERROR(651, "微信加密报文失败"),
    WX_DECRYPT_DATA_ERROR(652, "微信解密报文失败"),


    HTTP_SEND_REQ_ERROR(300, "请求外部服务错误"),
    ;
    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
    public PayException getPayException(){
        return new PayException(msg, code);
    }
    public static PayException getPayException(String message){
        return new PayException(message, ErrorCode.FAILURE.getCode());
    }
    public BaseException getBaseException(){
        return new BaseException(msg, code);
    }
}
