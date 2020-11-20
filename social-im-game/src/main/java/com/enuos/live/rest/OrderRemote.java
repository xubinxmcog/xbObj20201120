package com.enuos.live.rest;

import com.enuos.live.rest.fallback.OrderRemoteFallback;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TODO 订单中心.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version V2.0.0
 * @since 2020/7/30 21:28
 */

@Component
@FeignClient(contextId = "orderTelnet", name = "SOCIAL-IM-ORDER", fallback = OrderRemoteFallback.class)
public interface OrderRemote {

  /**
   * TODO 游戏饰品.
   *
   * @param userId [用户ID]
   * @param gameCode [游戏编码]
   * @return [饰品列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/7/30 21:28
   */
  @GetMapping("/productBackpack/gameDecorate")
  List gameDecorate(@RequestParam("userId") Long userId, @RequestParam("gameCode") Integer gameCode);
}
