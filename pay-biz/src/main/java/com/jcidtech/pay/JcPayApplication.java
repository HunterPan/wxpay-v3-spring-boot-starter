package com.jcidtech.pay;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springblade.core.launch.BladeApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class JcPayApplication {

    public static void main(String[] args) {
        BladeApplication.run("jcpay",JcPayApplication.class, args);
    }

    @Bean
    public ApplicationRunner getApplicationRunner(){
       return new ApplicationRunner() {
           @Override
           public void run(ApplicationArguments args) throws Exception {
                log.info("start run success");
           }
       };
    }
}
