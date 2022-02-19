package com.jcidtech.pay.configureation;

import com.jcidtech.pay.properties.JcProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JcAutoConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "jc")
    @ConditionalOnMissingBean
    public JcProperties jcProperties() {
        return new JcProperties();
    }

}
