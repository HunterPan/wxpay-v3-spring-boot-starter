package com.jcidtech.pay.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcidech.mp.base.BaseService;
import com.jcidtech.pay.common.dto.OrderQuery;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.common.vo.OrderDetailVO;
import org.springblade.core.tool.tuple.Pair;

public interface IOrderService extends BaseService<Order> {
    Pair<Order, OrderDetail> buildJcOrder(ItemEntity item, PayChannel payChannel);
    void removeCreateErrorOrder(Pair<Order,OrderDetail> order);
    Page<OrderDetailVO> list(OrderQuery query);
    OrderDetailVO detail(OrderQuery query);
    Order getByOutTradeNo(String outTradeNo);
}
