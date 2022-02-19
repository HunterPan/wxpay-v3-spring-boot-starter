package com.jcidtech.pay.wx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcidtech.pay.wx.base.Sign;
import com.jcidtech.pay.wx.base.Validator;
import com.jcidtech.pay.wx.properties.WxProperties;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

public class WxPayV3RefundService extends WxPayV3Service{

    public WxPayV3RefundService(OkHttpClient okHttpClient, ObjectMapper objectMapper, Validator validator, WxProperties wxProperties, Sign sign) {
        super(okHttpClient, objectMapper, validator, wxProperties, sign);
    }
}
