package com.enuos.live.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName PetsTask
 * @Description: TODO 宠物任务注解
 * @Author xubin
 * @Date 2020/10/22
 * @Version V2.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PetsTask {

    String describe() default "";

    int task();
}
