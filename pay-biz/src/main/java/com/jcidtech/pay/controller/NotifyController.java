package com.jcidtech.pay.controller;

import com.alibaba.fastjson.JSONObject;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.paypal.dto.BaseNotify;
import com.jcidtech.pay.paypal.dto.PayCancelNotify;
import com.jcidtech.pay.paypal.dto.PaySuccessNotify;
import com.jcidtech.pay.paypal.enums.PayPalNotifyType;
import com.jcidtech.pay.service.unionpay.PayPalPay;
import com.jcidtech.pay.service.unionpay.UnionPayService;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.utils.SpringUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping
@Slf4j
public class NotifyController {

    @PostMapping("wx/notify")
    @ResponseBody
    public String wxPayNotify(HttpServletRequest request,@RequestBody String data){
        log.info("receive wx pay notify result:{}",data);
        UnionPayService unionPayService = SpringUtil.getBean(PayChannel.WX.getBeanName());
        Boolean result = unionPayService.dealPayNotifyResult(request,data);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",result?"SUCCESS":"FAILURE");
        jsonObject.put("message",result?"":"系统错误");
        return jsonObject.toJSONString();
    }

    /**
     * 扣款成功callback webhook
     * @param request
     * @param data
     * @param response
     */
    @PostMapping("paypal/notify")
    @ResponseBody
    public void payPalNotify(HttpServletRequest request,@RequestBody  String data,HttpServletResponse response){
        log.info("receive palPay notify result:{}",data);
        JSONObject jsonObject = JSONObject.parseObject(data);
        BaseNotify baseNotify = new BaseNotify();
        baseNotify.setNotifyType(PayPalNotifyType.PAYMENT_CAPTURE_COMPLETED);
        baseNotify.setContent(data);
        String orderId = "";
        if("CHECKOUT.ORDER.APPROVED".equals(jsonObject.getString("event_type"))){
            orderId = jsonObject.getJSONObject("resource").getString("id");
        }
        if("PAYMENT.CAPTURE.COMPLETED".equals(jsonObject.getString("event_type"))){
            orderId = jsonObject.getJSONObject("resource").getJSONObject("supplementary_data").getJSONObject("related_ids").getString("order_id");
        }
        baseNotify.setOutTradeNo(orderId);
        UnionPayService unionPayService = SpringUtil.getBean(PayChannel.PAYPAL.getBeanName());
        try {
            Boolean result = unionPayService.dealPayNotifyResult(request, baseNotify);
            response.setStatus(200);
        }catch (Exception e){
            response.setStatus(500);
        }
    }

    // 同步支付结果
    /**
     * 确认成功后跳转
     */
    @GetMapping("paypal/success")
    @ResponseBody
    public String payPalSuccess(HttpServletRequest request,@RequestParam("token") String token,@RequestParam("PayerID") String payerId, HttpServletResponse response){
        PaySuccessNotify paySuccessNotify = new PaySuccessNotify();
        paySuccessNotify.setPayerId(payerId);
        paySuccessNotify.setToken(token);
        log.info("receive payPal success notify result:{}",JSONObject.toJSON(paySuccessNotify));
        BaseNotify baseNotify = new BaseNotify();
        baseNotify.setNotifyType(PayPalNotifyType.ORDER_APPROVED);
        baseNotify.setContent(paySuccessNotify);
        baseNotify.setOutTradeNo(token);
        UnionPayService unionPayService = SpringUtil.getBean(PayChannel.PAYPAL.getBeanName());
        try {
            unionPayService.dealPayNotifyResult(request,baseNotify);
        }catch (Exception e){
            log.error("deal paySuccess error,token:{}",token);
            response.setStatus(500);
        }
        return "<h3>YOU HAVE PAY SUCCESS!<h3>";
    }
    /**
     * 取消成功后跳转
     */
    @GetMapping("paypal/cancel")
    @ResponseBody
    public String payPalCancel(HttpServletRequest request,@RequestParam("token") String token,@RequestParam("PayerID") String payerId, HttpServletResponse response){
        PayCancelNotify payCancelNotify = new PayCancelNotify();
        payCancelNotify.setPayerId(payerId);
        payCancelNotify.setToken(token);
        log.info("receive payPal cancel notify result:{}",JSONObject.toJSON(payCancelNotify));
        BaseNotify baseNotify = new BaseNotify();
        baseNotify.setNotifyType(PayPalNotifyType.ORDER_CANCEL);
        baseNotify.setContent(payCancelNotify);
        baseNotify.setOutTradeNo(token);
        UnionPayService unionPayService = SpringUtil.getBean(PayChannel.PAYPAL.getBeanName());
        try {
            unionPayService.dealPayNotifyResult(request,baseNotify);
        }catch (Exception e){
            log.error("deal payCancel error,token:{}",token);
            response.setStatus(500);
        }
        return "<h3>YOU HAVE CANCEL PAY!<h3>";
    }
}
