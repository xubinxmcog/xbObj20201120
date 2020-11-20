package com.enuos.live.rest;

import com.enuos.live.rest.fallback.IconRemoteFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TODO 第三方头像获取.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version V2.0.0
 * @since 2020/7/16 9:44
 */

@Component
@FeignClient(value = "api-service", url = "http://api.btstu.cn", fallback = IconRemoteFallback.class)
public interface IconRemote {

  /**
   * TODO 随机头像.
   *
   * @param method 输出壁纸端[mobile(手机端),pc（电脑端）,zsy（手机电脑自动判断）]默认为pc
   * @param format 输出壁纸格式[json|images]默认为images
   * @return [json]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/16 21:17
   * @update 2020/7/16 21:17
   */
  @RequestMapping(value = "/sjtx/api.php", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
  String getRandomIcon(@RequestParam("method") String method, @RequestParam("format") String format);

}
