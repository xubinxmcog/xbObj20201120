package com.enuos.live.utils;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO fastJson.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/4/2 14:55
 */
public class JsonUtils {

  /**
   * TODO Object>>>Map.
   *
   * @param object [数据对象]
   * @return [map]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/2 14:55
   * @update 2020/9/16 13:04
   */
  public static Map<String, Object> toObjectMap(Object object) {
    String jsonObjString = JSONObject.toJSONString(object);
    return JSONObject.parseObject(jsonObjString);
  }

  /**
   * TODO Object>>>List.
   *
   * @param object [数据对象]
   * @return [list]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/2 14:55
   * @update 2020/9/16 13:05
   */
  public static List<Map<String, Object>> listMap(Object object) {
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
   * TODO Object>>>List.
   *
   * @param object [数据对象]
   * @param beanType [对象类型]
   * @param <T> [类型]
   * @return [list]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/2 14:55
   * @update 2020/9/16 13:06
   */
  public static <T> List<T> toListType(Object object, Class<T> beanType) {
    String jsonObjString = JSONObject.toJSONString(object);
    return JSONObject.parseArray(jsonObjString, beanType);
  }

  /**
   * TODO Object>>>Pojo.
   *
   * @param object [数据对象]
   * @param beanType [对象类型]
   * @param <T> [类型]
   * @return [model]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/2 14:55
   * @update 2020/9/16 13:07
   */
  public static <T> T toObjectPojo(Object object, Class<T> beanType) {
    String jsonObjString = JSONObject.toJSONString(object);
    return JSONObject.parseObject(jsonObjString, beanType);
  }

}
