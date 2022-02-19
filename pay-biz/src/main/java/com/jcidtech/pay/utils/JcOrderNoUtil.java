package com.jcidtech.pay.utils;

import com.jcidtech.pay.common.entity.Order;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JcOrderNoUtil {

    public static String buildJcTradeNo(Order order){
        return order.getId().toString()+order.getDay();
    }
    public static Long buildJcTPayId(String jcTradeNo){
        try {
            return Long.valueOf(jcTradeNo.substring(0, jcTradeNo.length() - 8));
        }catch (Exception e){
            log.error("buildJcTPayId error,jcTradeNo:{}",jcTradeNo,e);
        }
        return null;
    }
}
