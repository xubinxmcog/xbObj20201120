package com.enuos.live.pojo;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * @Description service
 * @Author wangyingjie
 * @Date 2020/8/6
 * @Modified
 */
public class BaseService {

    private static final Integer INTEGER = 0;

    private static final Long LONG = 0L;

    private static final BigDecimal BIGDECIMAL = BigDecimal.ZERO.setScale(2);
    
    /** 
     * @Description: 给当前bean赋值，不涉及当前bean父类
     * @Param: [t] 
     * @Return: T 
     * @Author: wangyingjie
     * @Date: 2020/8/6 
     */ 
    public static <T> T initPropertyNumber(T t) {
        Field[] fields = t.getClass().getDeclaredFields();
        Field field;
        try {
            for (int i = 0; i < fields.length; i++) {
                field = fields[i];
                // 操作私有变量
                field.setAccessible(true);
                if (field.getType().equals(Integer.class)) {
                    field.set(t, INTEGER);
                } else if (field.getType().equals(Long.class)) {
                    field.set(t, LONG);
                } else if (field.getType().equals(BigDecimal.class)) {
                    field.set(t, BIGDECIMAL);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return t;
    }
}
