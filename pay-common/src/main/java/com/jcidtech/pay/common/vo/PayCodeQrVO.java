package com.jcidtech.pay.common.vo;

import com.jcidtech.pay.common.dto.BaseDTO;
import lombok.Data;

@Data
public class PayCodeQrVO extends BaseDTO {
    private String qrCode;
    private Long orderId;
}
