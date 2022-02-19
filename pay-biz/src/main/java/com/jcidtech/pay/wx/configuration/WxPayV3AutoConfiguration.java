package com.jcidtech.pay.wx.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcidtech.pay.service.IOrderService;
import com.jcidtech.pay.service.IPayChannelReturnResultService;
import com.jcidtech.pay.service.IScheduleJobService;
import com.jcidtech.pay.service.unionpay.WxPay;
import com.jcidtech.pay.wx.base.*;
import com.jcidtech.pay.wx.properties.WxProperties;
import com.jcidtech.pay.wx.service.WxPayV3RefundService;
import com.jcidtech.pay.wx.service.WxPayV3Service;
import com.jcidtech.pay.wx.util.OkHttpClientBuilderUtil;
import com.jcidtech.pay.wx.util.PemUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@Configuration
@ConditionalOnProperty(name="wx.need",havingValue = "true")
public class WxPayV3AutoConfiguration {

    private final ObjectMapper objectMapper;

    public WxPayV3AutoConfiguration(ObjectProvider<ObjectMapper> objectProvider) {
        this.objectMapper = objectProvider.getIfAvailable(ObjectMapper::new);
    }

    @Bean
    @ConfigurationProperties(prefix = "wx")
    public WxProperties wxProperties() {
        return new WxProperties();
    }

    @Bean
    public Sign wxPaySign(WxProperties wxProperties) throws IOException {
        PrivateKey privateKey = PemUtil.loadPrivateKey(new ClassPathResource(wxProperties.getPay().getPrivateKeyPath().replace("classpath:", "")).getInputStream());
        return new DefaultV3Sign(privateKey);
    }

    @Bean
    public Credentials wxPayCredentials(Sign sign) {
        return new DefaultV3Credentials(sign);
    }

    @Bean
    public WxPayV3OkHttpInterceptor wxPayInterceptor(Credentials credentials, WxProperties wxProperties) {
        return new WxPayV3OkHttpInterceptor(credentials, wxProperties.getPay().getMchId(),
                wxProperties.getPay().getCertificateSerialNo());
    }

    @Bean
    public Validator wxPayValidator(WxPayV3OkHttpInterceptor wxPayV3OkHttpInterceptor, WxProperties wxProperties) {
        DefaultCertificatesVerifier defaultCertificatesVerifier
                = new DefaultCertificatesVerifier(wxProperties.getPay().getApiV3Key().getBytes(StandardCharsets.UTF_8),
                OkHttpClientBuilderUtil.wxPayOkHttpClient(wxPayV3OkHttpInterceptor).build());
        return new DefaultV3Validator(defaultCertificatesVerifier);
    }

    @Bean
    public WxPayV3Service wxPayV3Service(WxPayV3OkHttpInterceptor wxPayV3OkHttpInterceptor,
                                         Validator validator,
                                         Sign sign, WxProperties wxProperties) {
        return new WxPayV3Service(OkHttpClientBuilderUtil.wxPayOkHttpClient(wxPayV3OkHttpInterceptor).build(),
                objectMapper, validator, wxProperties, sign);
    }
    @Bean
    public WxPayV3RefundService wxPayV3RefundService(WxPayV3OkHttpInterceptor wxPayV3OkHttpInterceptor,
                                                     Validator validator,
                                                     Sign sign, WxProperties wxProperties) {
        return new WxPayV3RefundService(OkHttpClientBuilderUtil.wxPayOkHttpClientWithSsl(wxPayV3OkHttpInterceptor,wxProperties.getPay().getMchId(),wxProperties.getPay().getCertP12Path()).build(),
                objectMapper, validator, wxProperties, sign);
    }

    @Bean
    public WxPay wxPay(WxPayV3Service wxPayV3Service, IOrderService iOrderService,
                       IScheduleJobService scheduleJobService, IPayChannelReturnResultService payChannelReturnResultService) {
        return new WxPay(iOrderService,scheduleJobService,payChannelReturnResultService,wxPayV3Service);
    }
}
