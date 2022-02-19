package com.jcidtech.pay.service.after;

import com.jcidtech.pay.common.entity.Order;

public interface PaySuccessHandler {
    void process(Order order);
}
