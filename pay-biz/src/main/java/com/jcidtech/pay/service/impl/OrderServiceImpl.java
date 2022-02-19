package com.jcidtech.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beust.jcommander.internal.Lists;
import com.jcidech.mp.base.BaseServiceImpl;
import com.jcidech.mp.support.Condition;
import com.jcidtech.pay.common.dto.OrderQuery;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.entity.PayScheduleJob;
import com.jcidtech.pay.common.enums.OrderStatus;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.common.enums.PayCurrency;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.common.vo.OrderDetailVO;
import com.jcidtech.pay.mapper.OrderDetailMapper;
import com.jcidtech.pay.mapper.OrderMapper;
import com.jcidtech.pay.mapper.PayScheduleJobMapper;
import com.jcidtech.pay.properties.JcProperties;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.utils.JcOrderNoUtil;
import com.jcidtech.pay.wx.dto.WxCreateOrderRequest;
import com.jcidtech.pay.wx.enums.OrderType;
import com.jcidtech.pay.wx.service.WxPayV3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.tuple.Pair;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements IOrderService {
    private final OrderDetailMapper orderDetailMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private TransactionDefinition transactionDefinition;

    //第三方调用失败删除
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCreateErrorOrder(Pair<Order, OrderDetail> order) {
        this.deleteLogic(Arrays.asList(order.getLeft().getId()));
        orderDetailMapper.deleteById(order.getRight().getId());
    }

    @Override
    public Pair<Order, OrderDetail> buildJcOrder(ItemEntity item, PayChannel payChannel) {
        Long itemAmount = item.getPrice();
        Long discountAmt = item.getDiscount();
        Integer payAmount = (itemAmount.intValue() - discountAmt.intValue());
        if (payAmount < 0) {
            throw ErrorCode.ITEM_PAY_AMOUNT_VALID.getBaseException();
        }
       return  transactionTemplate.execute(new TransactionCallback<Pair<Order, OrderDetail>>() {
            @Override
            public Pair<Order, OrderDetail> doInTransaction(TransactionStatus transactionStatus) {
                Order order = new Order();
                order.setPayAmount(payAmount);
                order.setDiscountAmount(discountAmt.intValue());
                order.setTotalAmount(itemAmount.intValue());
                order.setStatus(OrderStatus.INIT.getValue());
                order.setDay(DateUtil.today());
                order.setPayChannel(payChannel.getChannel());
                save(order);
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(order.getId());
                orderDetail.setDiscountAmount(discountAmt);
                orderDetail.setTotalAmount(itemAmount);
                orderDetail.setPayAmount(payAmount.longValue());
                orderDetail.setItemId(item.getId());
                orderDetail.setItemTitle(item.getTitle());
                orderDetailMapper.insert(orderDetail);
                Pair<Order, OrderDetail> pair = Pair.create(order, orderDetail);
                return pair;
            }
        });
    }

    @Override
    public Page<OrderDetailVO> list(OrderQuery query) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().gt(Order::getCreateTime, query.getStartTime()).lt(Order::getCreateTime, query.getEndTime());
        if (Objects.nonNull(query.getId())) {
            queryWrapper.eq("id", query.getId());
        }
        if (StringUtil.isNotBlank(query.getPayer())) {
            queryWrapper.eq("payer", query.getPayer());
        }
        if (Objects.nonNull(query.getStatus())) {
            queryWrapper.eq("status", OrderStatus.getByStatus(query.getStatus()).getValue());
        }
        queryWrapper.orderByDesc("create_time");
        Page<OrderDetailVO> orderDetailVOPage = new Page<>();
        orderDetailVOPage.setCurrent(query.getCurrent());
        orderDetailVOPage.setSize(query.getSize());
        IPage<Order> orderList = this.page(Condition.getPage(query), queryWrapper);
        orderDetailVOPage.setTotal(orderList.getTotal());
        if (CollectionUtil.isEmpty(orderList.getRecords())) {
            return orderDetailVOPage;
        }
        List<Long> orderPayIds = orderList.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        QueryWrapper<OrderDetail> queryDetailWrapper = new QueryWrapper<>();
        queryDetailWrapper.lambda().in(OrderDetail::getOrderId, orderPayIds);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryDetailWrapper);
        Map<Long, OrderDetail> orderDetailMap = orderDetailList.stream().collect(Collectors.toMap(OrderDetail::getOrderId, Function.identity()));
        List<OrderDetailVO> orderDetailVOList = Lists.newArrayList(orderPayIds.size());
        for (Order order : orderList.getRecords()) {
            OrderDetailVO orderDetailVO = transfer2OrderDetailVO(order, orderDetailMap.get(order.getId()));
            orderDetailVOList.add(orderDetailVO);
        }
        orderDetailVOPage.setRecords(orderDetailVOList);
        return orderDetailVOPage;
    }

    private OrderDetailVO transfer2OrderDetailVO(Order order, OrderDetail orderDetail) {
        OrderDetailVO orderDetailVO = BeanUtil.copy(order, OrderDetailVO.class);
        orderDetailVO.setPayChannelName(PayChannel.getByChannel(order.getPayChannel()).getRemark());
        OrderStatus orderStatus = OrderStatus.getByStatus(order.getStatus());
        if (Objects.nonNull(orderStatus)) {
            orderDetailVO.setStatusName(OrderStatus.getByStatus(order.getStatus()).getRemark());
        } else {
            orderDetailVO.setStatus(OrderStatus.UNKNOWN.getValue());
        }
        if (Objects.nonNull(orderDetail)) {
            orderDetailVO.setItemId(orderDetail.getItemId());
            orderDetailVO.setItemTitle(orderDetail.getItemTitle());
        }
        return orderDetailVO;
    }

    @Override
    public OrderDetailVO detail(OrderQuery query) {
        Order order = getById(query.getId());
        if (Objects.isNull(order)) {
            return null;
        }
        OrderDetail orderDetail = orderDetailMapper.selectOne(Wrappers.<OrderDetail>query().lambda().eq(OrderDetail::getOrderId, order.getId()));
        return transfer2OrderDetailVO(order, orderDetail);
    }

    @Override
    public Order getByOutTradeNo(String outTradeNo) {
        Order order = getOne(Wrappers.<Order>query().lambda().eq(Order::getOutTradeNo, outTradeNo));
        return order;
    }
}
