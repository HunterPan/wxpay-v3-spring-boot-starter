package com.jcidtech.pay.common.vo;

import com.jcidtech.pay.common.dto.BaseDTO;
import lombok.Data;

@Data
public class ItemForUserVO extends BaseDTO {
    private Long  id;
    //名称
    private String title;
    //价格
    private Long price;
    //折扣
    private Long discount;
    //图片
    private String imageUrl;
    private String remark;
}
