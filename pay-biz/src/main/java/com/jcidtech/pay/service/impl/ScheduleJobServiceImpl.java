package com.jcidtech.pay.service.impl;

;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.entity.PayScheduleJob;
import com.jcidtech.pay.common.enums.OrderStatus;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.common.enums.PayStatus;
import com.jcidtech.pay.mapper.PayScheduleJobMapper;
import com.jcidtech.pay.model.ScheduleJobQueryResult;
import com.jcidtech.pay.properties.JcProperties;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.IScheduleJobService;
import com.jcidtech.pay.service.unionpay.UnionPayService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.tuple.Pair;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleJobServiceImpl implements IScheduleJobService {
    private final PayScheduleJobMapper payScheduleJobMapper;
    private final IOrderService orderService;
    private final JcProperties jcProperties;
    @Override
    public void saveScheduleJob(Pair<Order, OrderDetail> order, PayChannel payChannel) {
        Date now = new Date();
        PayScheduleJob payScheduleJob = new PayScheduleJob();
        payScheduleJob.setPayChannel(payChannel.getChannel());
        payScheduleJob.setPayId(order.getLeft().getId());
        payScheduleJob.setCreateTime(new Date());
        payScheduleJob.setNextTime(new Date(now.getTime() + jcProperties.getJob().getIntervalTime()));
        payScheduleJob.setExpireTime(new Date(now.getTime() + jcProperties.getJob().getExpireTime()));
        payScheduleJobMapper.insert(payScheduleJob);
    }

    @Override
    public void removeScheduleJob(Order order) {
        payScheduleJobMapper.delete(Wrappers.<PayScheduleJob>query().lambda().eq(PayScheduleJob::getPayId,order.getId()));
    }

    //120s一次
    @Scheduled(fixedRate = 300000)
    public void compensationTask() {
        Date now = new Date();
        log.info("start job");
        List<PayScheduleJob> scheduleJobList = payScheduleJobMapper.selectList(Wrappers.<PayScheduleJob>query().lambda().le(PayScheduleJob::getNextTime, now).orderByDesc(PayScheduleJob::getNextTime));
        if (CollectionUtil.isEmpty(scheduleJobList)) {
            log.info("finish,no job");
            return;
        }
        for (PayScheduleJob job : scheduleJobList) {
            try {
                Order order = orderService.getById(job.getPayId());
                if (Objects.equals(order.getStatus(), OrderStatus.PAID.getValue())
                        || Objects.equals(order.getStatus(), OrderStatus.UN_PAID_FINISH.getValue())) {
                    log.warn("order has finish,payId:{},time:{}", order.getId(), order.getPayTime());
                    payScheduleJobMapper.deleteById(job.getId());
                    continue;
                }
                //超时关闭
                if (job.getExpireTime().getTime() < now.getTime()) {
                    log.warn("order time out close payId:{}", order.getId());
                    order.setStatus(OrderStatus.TIMEOUT_CLOSED.getValue());
                    order.setRemark(OrderStatus.TIMEOUT_CLOSED.getRemark());
                    payScheduleJobMapper.deleteById(job.getId());
                    continue;
                }
                PayChannel payChannel = PayChannel.getByChannel(job.getPayChannel());
                UnionPayService unionPayService = SpringUtil.getBean(payChannel.getBeanName());
                if (Objects.isNull(unionPayService)) {
                    continue;
                }
                ScheduleJobQueryResult scheduleJobQueryResult = unionPayService.queryOrder(order);
                if (Objects.isNull(scheduleJobQueryResult)) {
                    continue;
                }
                Pair<PayStatus, String> payStatusStringPair = scheduleJobQueryResult.getFinalResult();
                if (Objects.isNull(payStatusStringPair)) {
                    continue;
                }
                if (Objects.equals(payStatusStringPair.getLeft(), PayStatus.SUCCESS)) {
                    order.setStatus(OrderStatus.PAID.getValue());
                    order.setRemark(OrderStatus.PAID.getRemark());
                } else if (Objects.equals(payStatusStringPair.getLeft(), PayStatus.ORDER_CLOSE)) {
                    order.setStatus(OrderStatus.UN_PAID_FINISH.getValue());
                    order.setRemark(OrderStatus.UN_PAID_FINISH.getRemark());
                } else {
                    order.setStatus(OrderStatus.INIT.getValue());
                    order.setRemark(OrderStatus.INIT.getRemark());
                    job.setNextTime(new Date(job.getNextTime().getTime() + jcProperties.getJob().getIntervalTime()));
                    payScheduleJobMapper.updateById(job);
                }

                orderService.updateById(order);
            } catch (Exception e) {
                job.setNextTime(new Date(job.getNextTime().getTime() + jcProperties.getJob().getIntervalTime()));
                payScheduleJobMapper.updateById(job);
                log.error("job query error,payId:{}", job.getPayId(), e);
            }
        }
        log.info("finish,job count:{},costTime:{}s", scheduleJobList.size(), (System.currentTimeMillis() - now.getTime()) / 1000);
    }
}
