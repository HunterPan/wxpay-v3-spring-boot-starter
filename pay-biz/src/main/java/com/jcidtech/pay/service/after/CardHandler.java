package com.jcidtech.pay.service.after;

import com.jcidtech.pay.common.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardHandler implements PaySuccessHandler{
    @Override
    public void process(Order order) {
        log.error("----------");

    }

}