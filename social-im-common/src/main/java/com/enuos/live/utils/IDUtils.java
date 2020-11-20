package com.enuos.live.utils;

import org.apache.commons.lang.RandomStringUtils;

/**
 * @Description id工具类
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
public class IDUtils {

    /**
     * @Description: 用户ID
     * @Param: []
     * @Return: java.lang.Long
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    public static Long generator() {
        return Long.valueOf(Long.valueOf(RandomStringUtils.randomNumeric(8)) + 100000000L);
    }

    /**
     * @Description: 校验ID
     * @Param: [id]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/9/25
     */
    public static boolean isNotNull(Long id) {
        return id != null && id > 0;
    }

    /**
     * @Description: 校验ID
     * @Param: [id]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/9/25
     */
    public static boolean isNotNull(Integer id) {
        return id != null && id > 0;
    }
    
    /** 
     * @Description: 校验ID 
     * @Param: [id] 
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/9/25 
     */ 
    public static boolean isNull(Long id) {
        return !isNotNull(id);
    }

    /** 
     * @Description: 校验ID
     * @Param: [id] 
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/9/25 
     */ 
    public static boolean isNull(Integer id) {
        return !isNotNull(id);
    }
}
