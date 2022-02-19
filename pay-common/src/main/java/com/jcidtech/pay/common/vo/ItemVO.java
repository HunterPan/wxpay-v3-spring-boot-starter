package com.jcidtech.pay.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.jcidtech.pay.common.dto.BaseDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ItemVO extends BaseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private  Long id;
    //名称
    private String title;
    //价格
    @JsonSerialize(using = ToStringSerializer.class)
    private Long price;
    @JsonSerialize(using = ToStringSerializer.class)
    //折扣
    private Long discount;
    //图片
    private String imageUrl;
    //
    private String statusName;
    //
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    //描述
    private String remark;
}
