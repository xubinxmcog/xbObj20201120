package com.enuos.live.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Jackson.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/9/30 12:45
 */
public class JsonUtil {

  private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

  private static ObjectMapper om = new ObjectMapper();

  static {

    // 对象的所有字段全部列入，还是其他的选项，可以忽略null等
    om.setSerializationInclusion(Include.ALWAYS);
    // 设置Date类型的序列化及反序列化格式
    om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // 忽略空Bean转json的错误
    om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    // 忽略未知属性，防止json字符串中存在，java对象中不存在对应属性的情况出现错误
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // 注册一个时间序列化及反序列化的处理模块，用于解决jdk8中localDateTime等的序列化问题
    om.registerModule(new JavaTimeModule());
  }

  /**
   * 对象 => json字符串
   *
   * @param obj 源对象
   */
  public static <T> String toJson(T obj) {

    String json = null;
    if (obj != null) {
      try {
        json = om.writeValueAsString(obj);
      } catch (JsonProcessingException e) {
        logger.warn(e.getMessage(), e);
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    return json;
  }

  /**
   * json字符串 => 对象
   *
   * @param json 源json串
   * @param clazz 对象类
   * @param <T> 泛型
   */
  public static <T> T parse(String json, Class<T> clazz) {

    return parse(json, clazz, null);
  }

  /**
   * json字符串 => 对象
   *
   * @param json 源json串
   * @param type 对象类型
   * @param <T> 泛型
   */
  public static <T> T parse(String json, TypeReference type) {

    return parse(json, null, type);
  }


  /**
   * json => 对象处理方法
   * <br>
   * 参数clazz和type必须一个为null，另一个不为null
   * <br>
   * 此方法不对外暴露，访问权限为private
   *
   * @param json 源json串
   * @param clazz 对象类
   * @param type 对象类型
   * @param <T> 泛型
   */
  private static <T> T parse(String json, Class<T> clazz, TypeReference type) {
    T obj = null;
    if (!StringUtils.isEmpty(json)) {
      try {
        if (clazz != null) {
          obj = om.readValue(json, clazz);
        } else {
          obj = om.readValue(json, type);
        }
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    return obj;
  }

  public static Map<String, Object> toStringMap(String jsonString) {
    Map<String, Object> objectMap = Collections.emptyMap();
    if (Objects.nonNull(jsonString)) {
      try {
        objectMap = om.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return objectMap;
  }

  public static Map<String, Object> toObjectMap(Object obj) {
    Map<String, Object> objectMap = Collections.emptyMap();
    if (Objects.nonNull(obj)) {
      try {
        String jsonString = om.writeValueAsString(obj);
        objectMap = om.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return objectMap;
  }

  public static List<Map<String, Object>> toListObjectMap(Object obj) {
    List<Map<String, Object>> mapList = Collections.emptyList();
    if (Objects.nonNull(obj)) {
      try {
        String jsonString = om.writeValueAsString(obj);
        mapList = om.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return mapList;
  }

  public static <T> List<T> toListType(Object obj, Class<T> clazz) {
    if (Objects.nonNull(obj)) {
      try {
        String jsonString = om.writeValueAsString(obj);
        JavaType jt = om.getTypeFactory().constructParametricType(ArrayList.class, clazz);
        return om.readValue(jsonString, jt);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return Collections.emptyList();
  }

}
