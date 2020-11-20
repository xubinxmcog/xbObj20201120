package com.enuos.live.util;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/2 14:55
 */
public class JsonUtils {

  /**
   * 将object转换成 Map<String, Object>
   *
   * @param object 数据对象
   * @return map
   */
  public static Map<String, Object> toObjectMap(Object object) {
    String jsonObjString = JSONObject.toJSONString(object);
    return JSONObject.parseObject(jsonObjString);
  }

  /**
   * 将object转换成 List<Map<String, Object>>
   *
   * @param object 数据对象
   * @return list
   */
  public static List<Map<String, Object>> toListMap(Object object) {
    String jsonObjString = JSONObject.toJSONString(object);
    List<Object> list = JSONObject.parseArray(jsonObjString);
    List<Map<String, Object>> newList = new ArrayList<>();
    for (Object o : list) {
      String jsonObj = JSONObject.toJSONString(o);
      Map<String, Object> tempMap = JSONObject.parseObject(jsonObj);
      newList.add(tempMap);
    }
    return newList;
  }

  /**
   * 将object转换成 List<Type>
   *
   * @param object 数据对象
   * @param beanType 对象类型
   * @param <T> 类型
   * @return list
   */
  public static <T> List<T> toListType(Object object, Class<T> beanType) {
    String jsonObjString = JSONObject.toJSONString(object);
    return JSONObject.parseArray(jsonObjString, beanType);
  }

  /**
   * 将object转换成 pojo
   *
   * @param object 数据对象
   * @param beanType 对象类型
   * @param <T> 类型
   * @return model
   */
  public static <T> T toObjectPojo(Object object, Class<T> beanType) {
    String jsonObjString = JSONObject.toJSONString(object);
    return JSONObject.parseObject(jsonObjString, beanType);
  }

}
