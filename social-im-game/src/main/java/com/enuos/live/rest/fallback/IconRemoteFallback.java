package com.enuos.live.rest.fallback;

import com.enuos.live.rest.IconRemote;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/16 9:47
 */
@Component
public class IconRemoteFallback implements IconRemote {

  @Override
  public String getRandomIcon(String method, String format) {
    return "https://7lestore.oss-cn-hangzhou.aliyuncs.com/header/e9b068f4aab848a385ae80af01719a16.jpeg";
  }
}
