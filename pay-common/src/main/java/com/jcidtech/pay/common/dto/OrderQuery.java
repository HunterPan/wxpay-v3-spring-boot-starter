package com.jcidtech.pay.common.dto;

import com.jcidech.mp.support.Query;
import lombok.Data;

import java.util.Date;

@Data
public class OrderQuery extends Query {
    private Long id;
    private String payer;
    private String payChannel;
    private Date startTime;
    private Date endTime;
    private Integer status;
}
