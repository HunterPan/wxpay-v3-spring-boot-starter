package com.jcidtech.pay.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcidtech.pay.common.entity.PayChannelReturnResult;
import com.jcidtech.pay.mapper.PayChannelReturnResultMapper;
import com.jcidtech.pay.service.IPayChannelReturnResultService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class PayChannelReturnResultServiceImpl implements IPayChannelReturnResultService {
    private final PayChannelReturnResultMapper payChannelReturnResultMapper;
    public void save(PayChannelReturnResult payChannelReturnResult){
        Date now = new Date();
        PayChannelReturnResult old = payChannelReturnResultMapper.selectOne(Wrappers.<PayChannelReturnResult>query().lambda().eq(PayChannelReturnResult::getPayId,payChannelReturnResult.getPayId()));
        if(Objects.nonNull(old)){
            old.setUpdateTime(now);
            old.setChannelDetail(payChannelReturnResult.getChannelDetail());
            payChannelReturnResultMapper.updateById(old);
        }else {
            payChannelReturnResult.setCreateTime(now);
            payChannelReturnResult.setUpdateTime(now);
            payChannelReturnResultMapper.insert(payChannelReturnResult);
        }
    }
}
