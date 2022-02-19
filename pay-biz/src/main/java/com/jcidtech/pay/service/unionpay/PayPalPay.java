package com.jcidtech.pay.service.unionpay;

import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.enums.OrderStatus;
import com.jcidtech.pay.common.enums.PayStatus;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.model.ScheduleJobQueryResult;
import com.jcidtech.pay.paypal.dto.BaseNotify;
import com.jcidtech.pay.paypal.enums.PayPalOrderStatus;
import com.jcidtech.pay.paypal.enums.PayPalNotifyType;
import com.jcidtech.pay.paypal.service.PayPalService;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.IPayChannelReturnResultService;
import com.jcidtech.pay.service.IScheduleJobService;
import com.paypal.http.serializer.Json;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springblade.core.tool.tuple.Pair;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
public class PayPalPay extends UnionPayService{
    private PayPalService payPalService;
    public PayPalPay(IOrderService iOrderService, IScheduleJobService scheduleJobService, IPayChannelReturnResultService payChannelReturnResultService,
                     PayPalService payPalService) {
        super(iOrderService, scheduleJobService, payChannelReturnResultService);
        this.payPalService = payPalService;
    }
    @Override
    public String buildPayQr(Pair<Order, OrderDetail> orderDetailPair, ItemEntity item) {
        try {
            com.paypal.orders.Order payPalOrder = payPalService.createOrder(orderDetailPair.getLeft(),item);
            if(Objects.isNull(payPalOrder) || CollectionUtil.isEmpty(payPalOrder.links())){
                throw ErrorCode.ORDER_CREATE_NATIVE_ERROR.getPayException();
            }
            //out orderId
            if(StringUtil.isNotBlank(payPalOrder.id())){
                orderDetailPair.getLeft().setOutTradeNo(payPalOrder.id());
                iOrderService.updateById(orderDetailPair.getLeft());
            }
            LinkDescription linkDescription = payPalOrder.links().stream().filter(link->Objects.equals(link.rel(),"approve")).findFirst().orElse(null);
            return  linkDescription.href();
        }catch (Exception e){
            log.error("paypal create order error",e);
            throw ErrorCode.ORDER_CREATE_NATIVE_ERROR.getPayException();
        }
    }


    private com.paypal.orders.Order queryPayPalOrder(Order order){
        if(StringUtil.isBlank(order.getOutTradeNo())){
            return null;
        }
        int loop = 3;
        com.paypal.orders.Order payPalOrder = null;
        while(loop-->0) {
            try {
                payPalOrder = payPalService.getOrder(order.getOutTradeNo());
                log.info("paypal order status:{}", payPalOrder.status());
                if(Objects.equals(payPalOrder.status(), PayPalOrderStatus.APPROVED.getStatus())){
                    payPalOrder = payPalService.captureOrder(order.getOutTradeNo());
                }
                break;
            } catch (Exception e) {
                log.error("capture result error,order:{},outTradeNo:{}", order.getId(),order.getOutTradeNo(),e);
                if(e.getMessage().contains("ORDER_ALREADY_CAPTURED")){
                    try {
                        payPalOrder = payPalService.getOrder(order.getOutTradeNo());
                        log.info("paypal order status:{}", payPalOrder.status());
                    }catch (Exception e1){
                        log.error("capture result error,order:{},outTradeNo:{}",order.getId(),order.getOutTradeNo(), e);
                    }
                }
                if(Objects.nonNull(payPalOrder)){
                    break;
                }
                if(loop == 0){
                    log.error("query paypal order error,order:{},outTradeNo:{}",order.getId(),order.getOutTradeNo());
                }
            }
        }
        return payPalOrder;
    }
    @Override
    public ScheduleJobQueryResult queryChannelOrder(Order order) {
        if(StringUtil.isBlank(order.getOutTradeNo())){
            return null;
        }
        com.paypal.orders.Order payPalOrder = queryPayPalOrder(order);
        if(Objects.isNull(payPalOrder)){
            return null;
        }
        ScheduleJobQueryResult scheduleJobQueryResult = checkOrder(payPalOrder,order);
        return scheduleJobQueryResult;
    }
    private ScheduleJobQueryResult checkOrder(com.paypal.orders.Order payPalOrder,Order order){
        ScheduleJobQueryResult scheduleJobQueryResult = new ScheduleJobQueryResult();
        try {
            scheduleJobQueryResult.setChannelResult(new JSONObject(new Json().serialize(payPalOrder)).toString(4));
        }catch (Exception e){

        }
        scheduleJobQueryResult.setPayTime("");
        log.info("receive paypal trade no:{},trade status:{},jc orderId:{}",order.getId(),payPalOrder.status(),order.getOutTradeNo());
        if(payPalOrder.status().equals(PayPalOrderStatus.COMPLETED.getStatus()) && Objects.nonNull(payPalOrder.purchaseUnits().get(0).payments().captures())){
            scheduleJobQueryResult.setPayTime(payPalOrder.purchaseUnits().get(0).payments().captures().get(0).updateTime());
        }
        if(Objects.equals(payPalOrder.status(), PayPalOrderStatus.COMPLETED.getStatus())){
            scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.SUCCESS,payPalOrder.status()));
            return scheduleJobQueryResult;
        }
        scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.OTHER,payPalOrder.status()));
        return scheduleJobQueryResult;
    }
    @Override
    protected ScheduleJobQueryResult dealChannelPayNotifyResult(HttpServletRequest request, Object data) {
        BaseNotify notify = (BaseNotify)data;
        if(StringUtil.isBlank(notify.getOutTradeNo())){
            return null;
        }
        Order order = iOrderService.getByOutTradeNo(notify.getOutTradeNo());
        if(Objects.isNull(order)){
            log.error("order not exist,payPal trade no:{}",notify.getOutTradeNo());
            return null;
        }
        if(OrderStatus.PAID.getValue() == order.getStatus()){
            log.error("order has paid :{}",notify.getOutTradeNo());
            return null;
        }
        ScheduleJobQueryResult scheduleJobQueryResult = new ScheduleJobQueryResult();
        scheduleJobQueryResult.setOrder(order);
        if(notify.getNotifyType().equals(PayPalNotifyType.ORDER_CANCEL)){
            scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.ORDER_CLOSE,"CANCEL"));
            scheduleJobQueryResult.setChannelResult(data);
            return scheduleJobQueryResult;
        }
        if(notify.getNotifyType().equals(PayPalNotifyType.ORDER_APPROVED)){
            com.paypal.orders.Order payPalOrder = dealPayApproved(order);
            /*try {
                scheduleJobQueryResult.setChannelResult(new JSONObject(new Json().serialize(payPalOrder)).toString(4));
            }catch (Exception e){

            }
            if(Objects.equals(payPalOrder.status(), OrderStatus.COMPLETED.getStatus())){
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.SUCCESS,payPalOrder.status()));
                if(Objects.nonNull(payPalOrder.purchaseUnits().get(0).payments().captures())){
                    scheduleJobQueryResult.setPayTime(payPalOrder.purchaseUnits().get(0).payments().captures().get(0).updateTime());
                }
                return scheduleJobQueryResult;
            }else {
                scheduleJobQueryResult.setPayTime("");
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.OTHER,payPalOrder.status()));
            }*/
            scheduleJobQueryResult = checkOrder(payPalOrder,order);
            scheduleJobQueryResult.setOrder(order);
            return scheduleJobQueryResult;
        }
        if(notify.getNotifyType().equals(PayPalNotifyType.PAYMENT_CAPTURE_COMPLETED)){
            com.paypal.orders.Order payPalOrder = queryPayPalOrder(order);
            scheduleJobQueryResult = checkOrder(payPalOrder,order);
            scheduleJobQueryResult.setOrder(order);
            return scheduleJobQueryResult;
        }
        return null;
    }
    public com.paypal.orders.Order dealPayApproved(Order order) {
        if(StringUtil.isBlank(order.getOutTradeNo())){
            return null;
        }
        int loop = 3;
        com.paypal.orders.Order payPalOrder = null;
        while(loop-->0) {
            try {
                payPalOrder = payPalService.captureOrder(order.getOutTradeNo());
                log.info("paypal order status:{}", payPalOrder.status());
                payPalOrder = payPalService.getOrder(order.getOutTradeNo());
                log.info("paypal order status:{}", payPalOrder.status());
                break;
            } catch (Exception e) {
                log.error("capture result error,order:{},outTradeNo:{}", order.getId(),order.getOutTradeNo(),e);
                if(e.getMessage().contains("ORDER_ALREADY_CAPTURED")){
                    try {
                        payPalOrder = payPalService.getOrder(order.getOutTradeNo());
                        log.info("paypal order status:{}", payPalOrder.status());
                    }catch (Exception e1){
                        log.error("capture result error,order:{},outTradeNo:{}",order.getId(),order.getOutTradeNo(), e);
                    }
                }
                if(Objects.nonNull(payPalOrder)){
                    break;
                }
                if(loop == 0){
                    log.error("query paypal order error,order:{},outTradeNo:{}",order.getId(),order.getOutTradeNo());
                }
            }
        }
        return payPalOrder;
    }
}
