package com.jcidtech.pay.webconf.interceptor;

import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.utils.SessionUtil;
import com.jcidtech.pay.webconf.SysAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Objects;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod) handler;
            SysAdmin sysAdmin = h.getMethod().getAnnotation(SysAdmin.class);
            if(Objects.nonNull(sysAdmin)){
                log.info("need sysAdmin");
                throw ErrorCode.NO_AUTH.getBaseException();
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        } finally {
            SessionUtil.removeUserInfo();
        }
    }
}
