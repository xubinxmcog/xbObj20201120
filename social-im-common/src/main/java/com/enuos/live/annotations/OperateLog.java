package com.enuos.live.annotations;


import java.lang.annotation.*;

/**
 * @ClassName HomeController
 * @Description: TODO 操作日志
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作信息，例如：“新增用户”
     */
    String operateMsg() default "";

    /**
     * 日志类型，例如：“新增”
     */
    String logType() default "";

}
