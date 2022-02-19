package com.jcidtech.pay.model;

import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.enums.PayStatus;
import lombok.Data;
import org.springblade.core.tool.tuple.Pair;

import java.io.Serializable;

@Data
public class ScheduleJobQueryResult implements Serializable {
    private Pair<PayStatus,String> finalResult;
    private Object channelResult;
    private String payTime;
    private Order order;

}
