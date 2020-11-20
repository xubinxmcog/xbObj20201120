package com.enuos.live.server;

import com.enuos.live.utils.annotation.AbstractAction;
import com.google.common.collect.Maps;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * TODO 工厂处理方法.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/11 14:33
 */

@Component
public class HandlerProcessor implements BeanFactoryPostProcessor {

  private static final String HANDLER_PACKAGE = "com.enuos.live";

  @Override
  public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    Map<Integer, Class> handlerMap = Maps.newHashMapWithExpectedSize(3);
    ClassScanner.scan(HANDLER_PACKAGE, AbstractAction.class).forEach(clazz -> {
      // 获取注解中的类型值
      int code = clazz.getAnnotation(AbstractAction.class).cmd();
      // 将注解中的类型值作为key，对应的类作为value，保存在Map中
      handlerMap.put(code, clazz);
    });
    // HandlerContext，将其注册到spring容器中
    HandlerContext context = new HandlerContext(handlerMap);
    beanFactory.registerSingleton(HandlerContext.class.getName(), context);
  }
}
