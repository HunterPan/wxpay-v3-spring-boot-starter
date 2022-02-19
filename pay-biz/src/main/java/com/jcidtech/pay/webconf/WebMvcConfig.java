package com.jcidtech.pay.webconf;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.jcidtech.pay.common.error.BaseException;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.common.error.PayException;
import com.jcidtech.pay.webconf.interceptor.AuthInterceptor;
import com.jcidtech.pay.webconf.interceptor.TimeInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Configuration

public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private TimeInterceptor timeInterceptor;
    @Resource
    private AuthInterceptor authInterceptor;

    @Bean
    protected ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder){

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(BigDecimal.class,new BigDecimalJsonDeserializer());
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(timeInterceptor);
        registry.addInterceptor(authInterceptor);
    }
}
