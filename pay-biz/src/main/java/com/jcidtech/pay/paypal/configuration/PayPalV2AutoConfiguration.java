package com.jcidtech.pay.paypal.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcidtech.pay.paypal.base.PayPalClient;
import com.jcidtech.pay.paypal.properties.PayPalProperties;
import com.jcidtech.pay.paypal.service.PayPalService;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.IPayChannelReturnResultService;
import com.jcidtech.pay.service.IScheduleJobService;
import com.jcidtech.pay.service.unionpay.PayPalPay;
import com.jcidtech.pay.service.unionpay.WxPay;
import com.jcidtech.pay.wx.base.*;
import com.jcidtech.pay.wx.properties.WxProperties;
import com.jcidtech.pay.wx.service.WxPayV3RefundService;
import com.jcidtech.pay.wx.service.WxPayV3Service;
import com.jcidtech.pay.wx.util.OkHttpClientBuilderUtil;
import com.jcidtech.pay.wx.util.PemUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@Configuration
@ConditionalOnProperty(name="paypal.need",havingValue = "true")
public class PayPalV2AutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "paypal")
    public PayPalProperties payPalProperties() {
        return new PayPalProperties();
    }

    @Bean
    public PayPalService payPalService(PayPalProperties payPalProperties) {
        return new PayPalService(new PayPalClient(payPalProperties));
    }
    @Bean
    public PayPalPay payPalPay(IOrderService iOrderService,
                           IScheduleJobService scheduleJobService, IPayChannelReturnResultService payChannelReturnResultService,
                           PayPalService payPalService) {
        return new PayPalPay(iOrderService,scheduleJobService,payChannelReturnResultService,payPalService);
    }
}
