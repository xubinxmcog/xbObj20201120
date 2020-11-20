package com.enuos.live.server;

import java.util.Objects;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * TODO BEAN工具类.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/11 15:10
 */

@Component
@SuppressWarnings("WeakerAccess")
public class BeanTool implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
    if (Objects.isNull(applicationContext)) {
      applicationContext = context;
    }
  }

  public static Object getBean(String name) {
    return applicationContext.getBean(name);
  }


  public static <T> T getBean(Class<T> clazz) {
    return applicationContext.getBean(clazz);
  }
}
