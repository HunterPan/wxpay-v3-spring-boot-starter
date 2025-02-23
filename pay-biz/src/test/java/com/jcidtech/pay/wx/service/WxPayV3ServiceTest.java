package com.jcidtech.pay.wx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcidtech.pay.wx.base.WxPayException;
import com.jcidtech.pay.wx.dto.WxCreateOrderRequest;
import com.jcidtech.pay.wx.dto.WxPayResult;
import com.jcidtech.pay.wx.dto.WxRefundRequest;
import com.jcidtech.pay.wx.enums.OrderType;
import com.jcidtech.pay.wx.properties.WxProperties;
import com.jcidtech.pay.wx.service.WxPayV3Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.ResourceBundle;
import java.util.UUID;

//@SpringBootTest
//@ActiveProfiles("test")
class WxPayV3ServiceTest {

    //@Resource
    private WxPayV3Service wxPayV3Service;

    //@Resource
    private WxProperties wxProperties;

   // @Test
    void nativeCreateOrder() throws WxPayException {
        WxCreateOrderRequest request = new WxCreateOrderRequest();
        request.setOrderType(OrderType.jsapi);
        request.setDescription("测试商品");
        request.setOutTradeNo(UUID.randomUUID().toString().replaceAll("-", ""));
        WxCreateOrderRequest.Amount amount = new WxCreateOrderRequest.Amount();
        amount.setTotal(10);
        request.setAmount(amount);
        WxCreateOrderRequest.Payer payer = new WxCreateOrderRequest.Payer();
        payer.setOpenid("oT5Pk5GxcjYfGQ-MCLi0QRp45Quc");
        request.setPayer(payer);
        String prepay_id = wxPayV3Service.createOrder(request);
        System.out.println("prepay_id:" + prepay_id);
        Assertions.assertNotNull(prepay_id);

    }

    //@Test
   // @Disabled
    void refund() throws WxPayException {
        WxRefundRequest refundRequest = new WxRefundRequest();
        refundRequest.setNotifyUrl(wxProperties.getPay().getRefundNotifyUrl());
        WxRefundRequest.Amount amount = new WxRefundRequest.Amount();
        amount.setTotal(1);
        amount.setRefund(1);
        refundRequest.setAmount(amount);
        refundRequest.setOutRefundNo(UUID.randomUUID().toString());
        wxPayV3Service.refund(refundRequest);
    }

   // @BeforeAll
    static void before() {
        Assertions.assertDoesNotThrow(() -> ResourceBundle.getBundle("application-test"), "未找到application-test.properties文件！");
    }

    //@Test
    void queryOrderByTransactionsId() throws WxPayException, JsonProcessingException {
        WxPayResult wxPayResult = wxPayV3Service.queryOrderByTransactionsId("4200000598202006225181965671");
        System.out.println(new ObjectMapper().writeValueAsString(wxPayResult));
    }

    //@Test
    void queryOrderByOutTradeId() throws WxPayException, JsonProcessingException {
        WxPayResult wxPayResult = wxPayV3Service.queryOrderByOutTradeId("1000000020200622102353");
        System.out.println(new ObjectMapper().writeValueAsString(wxPayResult));
    }

   // @Test
    void closeOrder() throws WxPayException {
        wxPayV3Service.closeOrder("1000000020200617085523");
    }
}
