package com.jcidtech.pay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcidtech.pay.common.dto.CreateOrderDTO;
import com.jcidtech.pay.common.dto.OrderQuery;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.OrderDetail;
import com.jcidtech.pay.common.enums.ItemStatus;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.common.vo.OrderDetailVO;
import com.jcidtech.pay.common.vo.PayCodeQrVO;
import com.jcidtech.pay.service.IItemService;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.unionpay.UnionPayService;
import com.jcidtech.pay.utils.QRCodeUtil;
import com.jcidtech.pay.webconf.SysAdmin;
import com.jcidtech.pay.wx.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("order")
@Slf4j
public class OrderController {
    @Resource
    private IOrderService orderService;
    @Resource
    private IItemService itemService;
    @PostMapping("/create-order")
    public R<PayCodeQrVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        //校验商品是否还在
        ItemEntity item = itemService.getById(createOrderDTO.getItemId());
        if(Objects.isNull(item) || Objects.equals(item.getStatus(),ItemStatus.OFF.getValue())){
            throw ErrorCode.ITEM_VALID.getBaseException();
        }
        PayChannel payChannelEnum = PayChannel.getByChannel(createOrderDTO.getPayChannel());
        UnionPayService unionPayService = SpringUtil.getBean(payChannelEnum.getBeanName());
        PayCodeQrVO codeUrl = unionPayService.buildOrder(item,payChannelEnum);
        log.info("create code url:{}，orderId:{}",codeUrl.getQrCode(),codeUrl.getOrderId());
        codeUrl.setQrCode(QRCodeUtil.generateQRCode(codeUrl.getQrCode(), payChannelEnum,300,300));
        return R.data(codeUrl);
    }
    @PostMapping("/paypal/create-order")
    public R<PayCodeQrVO> createPayPalOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        //校验商品是否还在
        ItemEntity item = itemService.getById(createOrderDTO.getItemId());
        if(Objects.isNull(item) || Objects.equals(item.getStatus(),ItemStatus.OFF.getValue())){
            throw ErrorCode.ITEM_VALID.getBaseException();
        }
        PayChannel payChannelEnum = PayChannel.PAYPAL;
        createOrderDTO.setPayChannel(payChannelEnum.getChannel());
        UnionPayService unionPayService = SpringUtil.getBean(payChannelEnum.getBeanName());
        PayCodeQrVO codeUrl = unionPayService.buildOrder(item,payChannelEnum);
        log.info("create code url:{}，orderId:{}",codeUrl.getQrCode(),codeUrl.getOrderId());
        return R.data(codeUrl);
    }
    @SysAdmin
    @PostMapping("/list")
    public  R<Page<OrderDetailVO>> list(OrderQuery query) {
        if(Objects.isNull(query)){
            query = new OrderQuery();
        }
        Date now = new Date();
        if(Objects.isNull(query.getStartTime())){
            query.setStartTime(DateTimeUtil.beforeDays(now,1));
        }
        if(Objects.isNull(query.getEndTime())){
            query.setEndTime(now);
        }
        return R.data(orderService.list(query));
    }
    @SysAdmin
    @GetMapping("/detail-for-admin")
    public  R<OrderDetailVO> detailForAdmin(OrderQuery query) {
        if(Objects.isNull(query)){
            throw ErrorCode.ILLEGAL_URL_PARAM.getBaseException();
        }
        Date now = new Date();
        if(Objects.isNull(query.getStartTime())){
            query.setStartTime(DateTimeUtil.beforeDays(now,1));
        }
        if(Objects.isNull(query.getEndTime())){
            query.setEndTime(now);
        }
        return R.data(orderService.detail(query));
    }
    @GetMapping("/detail-for-user")
    public  R<OrderDetailVO> detail(OrderQuery query) {
        if(Objects.isNull(query)){
            throw ErrorCode.ILLEGAL_URL_PARAM.getBaseException();
        }
        Date now = new Date();
        if(Objects.isNull(query.getStartTime())){
            query.setStartTime(DateTimeUtil.beforeDays(now,1));
        }
        if(Objects.isNull(query.getEndTime())){
            query.setEndTime(now);
        }
        return R.data(orderService.detail(query));
    }
}
