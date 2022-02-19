package com.jcidtech.pay.service.unionpay;

import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.enums.PayCurrency;
import com.jcidtech.pay.common.enums.PayStatus;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.model.ScheduleJobQueryResult;
import com.jcidtech.pay.paypal.dto.PaySuccessNotify;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.IPayChannelReturnResultService;
import com.jcidtech.pay.service.IScheduleJobService;
import com.jcidtech.pay.utils.JcOrderNoUtil;
import com.jcidtech.pay.wx.dto.WxCreateOrderRequest;
import com.jcidtech.pay.wx.dto.WxPayResult;
import com.jcidtech.pay.wx.enums.OrderType;
import com.jcidtech.pay.wx.enums.TradeState;
import com.jcidtech.pay.wx.service.WxPayV3Service;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.tuple.Pair;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
public class WxPay extends UnionPayService{
    @Resource
    private WxPayV3Service wxPayV3Service;

    public WxPay(IOrderService iOrderService, IScheduleJobService scheduleJobService,
                 IPayChannelReturnResultService payChannelReturnResultService, WxPayV3Service wxPayV3Service) {
        super(iOrderService, scheduleJobService, payChannelReturnResultService);
        this.wxPayV3Service = wxPayV3Service;
    }

    @Override
    public String buildPayQr(Pair<Order, OrderDetail> orderDetailPair, ItemEntity item) {
        WxCreateOrderRequest request = buildWxOrder(orderDetailPair,item);
        try {
            String prepayId = wxPayV3Service.createOrder(request);
            return prepayId;
        }catch (Exception e){
            log.error("create order error",e);
            throw ErrorCode.ORDER_CREATE_NATIVE_ERROR.getPayException();
        }
    }
    private WxCreateOrderRequest buildWxOrder(Pair<Order,OrderDetail> pair, ItemEntity item){
        WxCreateOrderRequest wxCreateOrderRequest = new WxCreateOrderRequest();
        wxCreateOrderRequest.setOrderType(OrderType.nativeS);
        wxCreateOrderRequest.setOutTradeNo(JcOrderNoUtil.buildJcTradeNo(pair.getLeft()));
        Long itemAmount = item.getPrice();
        Long discountAmt = item.getDiscount();
        Integer payAmount = (itemAmount.intValue()-discountAmt.intValue());
        if(payAmount < 0){
            throw ErrorCode.ITEM_PAY_AMOUNT_VALID.getBaseException();
        }
        WxCreateOrderRequest.Amount amount = new WxCreateOrderRequest.Amount();
        amount.setTotal(payAmount);
        amount.setCurrency(PayCurrency.CNY.getCurrency());
        wxCreateOrderRequest.setAmount(amount);
        wxCreateOrderRequest.setDescription(item.getTitle());
        return wxCreateOrderRequest;
    }

    @Override
    public ScheduleJobQueryResult queryChannelOrder(Order order) {
        ScheduleJobQueryResult scheduleJobQueryResult = null;
        String jcTradeNo = JcOrderNoUtil.buildJcTradeNo(order);
        try {
            WxPayResult wxPayResult = wxPayV3Service.queryOrderByJcTradeNo(jcTradeNo);
            scheduleJobQueryResult = checkPayResult(order,wxPayResult);
        }catch (Exception e){
            log.error("query wx order error,orderNo:{}",jcTradeNo,e);
        }
        return scheduleJobQueryResult;
    }
    private ScheduleJobQueryResult checkPayResult(Order order,WxPayResult wxPayResult){
        ScheduleJobQueryResult scheduleJobQueryResult = new ScheduleJobQueryResult();
        try {
            if(Objects.isNull(wxPayResult)){
                return null;
            }
            order.setOutTradeNo(wxPayResult.getTransactionId());
            scheduleJobQueryResult.setChannelResult(wxPayResult);
            scheduleJobQueryResult.setPayTime(wxPayResult.getSuccessTime());
            TradeState tradeState = wxPayResult.getTradeState();
            log.info("receive wx trade no:{},trade status:{},jc orderId:{}",wxPayResult.getTransactionId(),tradeState.getValue(),wxPayResult.getOutTradeNo());
            if(Objects.equals(tradeState,TradeState.SUCCESS)){
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.SUCCESS,tradeState.getValue()));
                order.setPayTime(wxPayResult.getSuccessTime());
                return scheduleJobQueryResult;
            }
            if(Objects.equals(tradeState,TradeState.CLOSED)){
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.ORDER_CLOSE,tradeState.getValue()));
                return scheduleJobQueryResult;
            }
            if(Objects.equals(tradeState,TradeState.REVOKED)){
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.ORDER_CLOSE,tradeState.getValue()));
                return scheduleJobQueryResult;
            }
            if(Objects.equals(tradeState,TradeState.PAYERROR)){
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.ORDER_CLOSE,tradeState.getValue()));
                return scheduleJobQueryResult;
            }
            if(Objects.equals(tradeState,TradeState.REFUND)){
                scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.ORDER_CLOSE,tradeState.getValue()));
                return scheduleJobQueryResult;
            }
            scheduleJobQueryResult.setFinalResult(Pair.create(PayStatus.OTHER,tradeState.getValue()));
            return scheduleJobQueryResult;
        }catch (Exception e){
            log.error("query wx order error,payId:{}",order.getId(),e);
        }
        return null;
    }
    @Override
    protected ScheduleJobQueryResult dealChannelPayNotifyResult(HttpServletRequest request, Object data) {
        WxPayResult wxPayResult = null;
        try {
            wxPayResult = wxPayV3Service.buildPayResult(request, (String)data);
            Order order = iOrderService.getById(JcOrderNoUtil.buildJcTPayId(wxPayResult.getOutTradeNo()));
            if(Objects.isNull(order)){
                log.error("order not exist,wx trade no:{}",wxPayResult.getTransactionId());
                return null;
            }
            log.info("receive wx trade no:{},trade status:{},jc orderId:{}",wxPayResult.getTransactionId(),wxPayResult.getTradeState().getValue(),wxPayResult.getOutTradeNo());
            ScheduleJobQueryResult scheduleJobQueryResult = checkPayResult(order,wxPayResult);
            if(Objects.isNull(scheduleJobQueryResult)){
                return null;
            }
            scheduleJobQueryResult.setOrder(order);
            return scheduleJobQueryResult;
        }catch (Exception e){
            log.error("deal wx pay result error,param:{}",data);
            return null;
        }
    }
}
