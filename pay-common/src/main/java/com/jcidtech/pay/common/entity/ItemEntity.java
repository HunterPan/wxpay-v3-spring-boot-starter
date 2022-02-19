package com.jcidtech.pay.common.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.jcidech.mp.base.BaseEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@TableName("tb_item")
@Data
public class ItemEntity extends BaseEntity {
    //名称
    private String title;
    //价格
    @JsonSerialize(using = ToStringSerializer.class)
    private Long price;
    //折扣
    @JsonSerialize(using = ToStringSerializer.class)
    private Long discount;

    private String remark;
    //分类
    private Integer category;
}
