package com.enuos.live.server;

import com.enuos.live.utils.annotation.AbstractActionHandler;
import java.util.Map;
import java.util.Objects;

/**
 * TODO 处理上下文.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/11 16:23
 */

public class HandlerContext {

  private Map<Integer, Class> handlerMap;

  public HandlerContext(Map<Integer, Class> handlerMap) {
    this.handlerMap = handlerMap;
  }

  public AbstractActionHandler getInstance(Integer cmd) {
    Class aClass = handlerMap.get(cmd);
    if (Objects.isNull(aClass)) {
      throw new IllegalArgumentException("[INSTANCE_SCAN GET_INSTANCE] ERROR: [" + cmd + "]");
    }
    return (AbstractActionHandler) BeanTool.getBean(aClass);
  }
}
