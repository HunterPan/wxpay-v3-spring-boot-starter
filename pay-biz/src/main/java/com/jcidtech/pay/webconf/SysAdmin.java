package com.jcidtech.pay.webconf;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SysAdmin {
    String[] value() default "";
}
