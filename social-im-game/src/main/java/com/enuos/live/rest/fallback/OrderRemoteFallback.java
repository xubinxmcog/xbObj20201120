package com.enuos.live.rest.fallback;

import com.enuos.live.rest.OrderRemote;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version V2.0.0
 * @since 2020/7/30 21:28
 */

@Component
public class OrderRemoteFallback implements OrderRemote {

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
  @Override
  public List gameDecorate(Long userId, Integer gameCode) {
    return null;
  }
}
