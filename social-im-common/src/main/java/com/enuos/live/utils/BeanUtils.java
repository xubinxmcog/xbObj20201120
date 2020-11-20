package com.enuos.live.utils;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @Description 对象工具类
 * @Author wangyingjie
 * @Date 10:33 2020/4/20
 * @Modified
 */
public class BeanUtils extends BeanUtil {

    /**
     * @Description: 复制对象忽略空属性
     * @Param: [source, target]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * @Description: 获取对象中属性值为null的属性名
     * @Param: [source]
     * @Return: java.lang.String[]
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * @Description: 深拷贝
     * @Param: [object 实现序列化的object, beanType 不可为抽象类及接口]
     * @Return: T
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    public static <T> T deepCopyByJson(Object object, Class<T> beanType) {
        return JSONObject.parseObject(JSONObject.toJSONString(object), beanType);
    }

}
