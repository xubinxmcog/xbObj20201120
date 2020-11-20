package com.enuos.live.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TODO 日志工具.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/10/22 9:39
 */

@Component
public class LoggerManager {

  private static Logger logger = LoggerFactory.getLogger(LoggerManager.class);

  public static void info(String msg, Object... args){
    logger.info(msg, args);
  }

  public static void error(String msg,Object... args){
    logger.error(msg, args);
  }

  public static void warn(String msg,Object... args){
    logger.warn(msg, args);
  }

}
