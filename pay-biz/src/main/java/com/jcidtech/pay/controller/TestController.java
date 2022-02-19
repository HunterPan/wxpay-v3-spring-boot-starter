package com.jcidtech.pay.controller;

import com.jcidtech.pay.service.after.PaySuccessHandler;
import com.jcidtech.pay.utils.JcOrderNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("demo")
@Slf4j
public class TestController {
    @GetMapping("health")
    @ResponseBody
    public String health() {
        return "ok";
    }

    @PostMapping("order")
    @ResponseBody
    public String testDemo() {
        return null;
    }
    @PostMapping("test")
    @ResponseBody
    public String test(@RequestBody String data) {
        try {
            return  JcOrderNoUtil.buildJcTPayId(data).toString();
        } catch (Exception e) {
            log.error("order error",e);
            return "failure";
        }
    }
    @PostMapping("refund")
    @ResponseBody
    public String testRefund() {
        try {
            for(Object obj:SpringUtil.getContext().getBeansOfType(PaySuccessHandler.class).values()){
                log.info("sss:{}",obj.getClass().getName());
            }
            return DateUtil.format(new Date(),"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        } catch (Exception e) {
            log.error("order error",e);
            return "failure";
        }
    }
    @GetMapping("query")
    @ResponseBody
    public String testQuery() {
        try {
        } catch (Exception e) {
            log.error("order error",e);
            return "failure";
        }
        return "ok";
    }
}
