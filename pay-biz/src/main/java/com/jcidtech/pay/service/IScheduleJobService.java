package com.jcidtech.pay.service;


import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.enums.PayChannel;
import org.springblade.core.tool.tuple.Pair;

public interface IScheduleJobService {
    void saveScheduleJob(Pair<Order, OrderDetail> order, PayChannel payChannel);
    void removeScheduleJob(Order order);
}
