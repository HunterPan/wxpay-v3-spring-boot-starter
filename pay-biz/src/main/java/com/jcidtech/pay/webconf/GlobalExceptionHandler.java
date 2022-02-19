package com.jcidtech.pay.webconf;

import com.alibaba.fastjson.JSONObject;
import com.jcidtech.pay.common.error.BaseException;
import com.jcidtech.pay.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackages = "com.jcidtech.pay.controller")
@Slf4j
public class GlobalExceptionHandler {
    // ⒈全局异常处理返回字符串
    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public String exception(BaseException e) {// 未知的异常做出响应
        return JSONObject.toJSONString(R.fail(e.getCode(),e.getMessage()));
    }
    // ⒈全局异常处理返回字符串
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String exception(Exception e) {
        log.error("error",e);
        return JSONObject.toJSONString(R.fail(ErrorCode.FAILURE.getCode(),ErrorCode.FAILURE.getMsg()));
    }
}
