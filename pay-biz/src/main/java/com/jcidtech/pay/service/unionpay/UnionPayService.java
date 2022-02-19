package com.jcidtech.pay.service.unionpay;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.entity.PayChannelReturnResult;
import com.jcidtech.pay.common.enums.OrderStatus;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.common.enums.PayStatus;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.common.error.PayException;
import com.jcidtech.pay.common.vo.PayCodeQrVO;
import com.jcidtech.pay.model.ScheduleJobQueryResult;
import com.jcidtech.pay.paypal.dto.PaySuccessNotify;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.IPayChannelReturnResultService;
import com.jcidtech.pay.service.IScheduleJobService;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.tuple.Pair;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
public abstract class UnionPayService {
    @Resource
    protected IOrderService iOrderService;
    @Resource
    private IScheduleJobService scheduleJobService;
    @Resource
    private IPayChannelReturnResultService payChannelReturnResultService;

    public UnionPayService(IOrderService iOrderService, IScheduleJobService scheduleJobService, IPayChannelReturnResultService payChannelReturnResultService) {
        this.iOrderService = iOrderService;
        this.scheduleJobService = scheduleJobService;
        this.payChannelReturnResultService = payChannelReturnResultService;
    }
    /**
     * 获取支付二维码
     * @param item
     * @param payChannel
     * @return
     */
    public PayCodeQrVO buildOrder(ItemEntity item, PayChannel payChannel){
        //为减少大事物，这里切分
        //创建订单
        Pair<Order, OrderDetail>  orderDetailPair = iOrderService.buildJcOrder(item,payChannel);
        try {
            //调用第三方获取支付二维码
            String codeUrl = buildPayQr(orderDetailPair, item);
            //保存定时任务定时查询
            scheduleJobService.saveScheduleJob(orderDetailPair,payChannel);
            PayCodeQrVO payCodeQrVO = new PayCodeQrVO();
            payCodeQrVO.setQrCode(codeUrl);
            payCodeQrVO.setOrderId(orderDetailPair.getLeft().getId());
            return payCodeQrVO;
        }catch (PayException e){
            //异常删除
            iOrderService.removeCreateErrorOrder(orderDetailPair);
            throw e;
        }catch (Exception e){
            //异常删除
            log.error("buildPayQr error,payId:{}", orderDetailPair.getLeft().getId(),e);
            iOrderService.removeCreateErrorOrder(orderDetailPair);
            throw ErrorCode.ORDER_CREATE_NATIVE_ERROR.getPayException();
        }
    }

    /**
     * 创建二维码
     * @param orderDetailPair
     * @param item
     * @return
     */
    public abstract String buildPayQr(Pair<Order, OrderDetail> orderDetailPair, ItemEntity item);

    /**
     * 查询订单支付结果
     * @param order
     * @return
     */
    public ScheduleJobQueryResult queryOrder(Order order){
        ScheduleJobQueryResult scheduleJobQueryResult = queryChannelOrder(order);
        if(Objects.isNull(scheduleJobQueryResult)){
            return null;
        }
        savePayResult(order,scheduleJobQueryResult.getChannelResult());
        return scheduleJobQueryResult;
    }
    private void savePayResult(Order order,Object channelResult){
        PayChannelReturnResult payChannelReturnResult = new PayChannelReturnResult();
        payChannelReturnResult.setPayId(order.getId());
        payChannelReturnResult.setOutTradeNo(order.getOutTradeNo());
        if(channelResult instanceof String){
            payChannelReturnResult.setChannelDetail((String) channelResult);
        }else{
            payChannelReturnResult.setChannelDetail(JsonUtil.toJson(channelResult));
        }
        payChannelReturnResultService.save(payChannelReturnResult);
    }
    public abstract ScheduleJobQueryResult queryChannelOrder(Order order);

    /**
     * 处理外部支付通知
     * @param request
     * @param data
     */
    public Boolean dealPayNotifyResult(HttpServletRequest request,Object data){
        ScheduleJobQueryResult scheduleJobQueryResult = dealChannelPayNotifyResult(request,data);
        if(Objects.isNull(scheduleJobQueryResult)){
            return Boolean.FALSE;
        }
        if(Objects.nonNull(scheduleJobQueryResult.getFinalResult()) && Objects.nonNull(scheduleJobQueryResult.getFinalResult().getLeft())){
            closeOrder(scheduleJobQueryResult);
        }
        //保存支付结果
        savePayResult(scheduleJobQueryResult.getOrder(),scheduleJobQueryResult.getChannelResult());
        return Boolean.TRUE;
    }
    //关闭订单
    private void closeOrder(ScheduleJobQueryResult scheduleJobQueryResult){
        Order order = scheduleJobQueryResult.getOrder();
        Pair<PayStatus, String> payStatusStringPair = scheduleJobQueryResult.getFinalResult();
        if (Objects.equals(payStatusStringPair.getLeft(), PayStatus.SUCCESS)) {
            scheduleJobService.removeScheduleJob(order);
            order.setStatus(OrderStatus.PAID.getValue());
            order.setRemark(OrderStatus.PAID.getRemark());
        } else if (Objects.equals(payStatusStringPair.getLeft(), PayStatus.ORDER_CLOSE)) {
            scheduleJobService.removeScheduleJob(order);
            order.setStatus(OrderStatus.UN_PAID_FINISH.getValue());
            order.setRemark(OrderStatus.UN_PAID_FINISH.getRemark());
        } else {
            order.setStatus(OrderStatus.INIT.getValue());
            order.setRemark(OrderStatus.INIT.getRemark());
        }
        iOrderService.updateById(order);
    }
    protected abstract ScheduleJobQueryResult dealChannelPayNotifyResult(HttpServletRequest request,Object data);

}
