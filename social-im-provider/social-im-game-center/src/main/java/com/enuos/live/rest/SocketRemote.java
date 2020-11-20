package com.enuos.live.rest;

import com.enuos.live.rest.fallback.SocketRemoteFallback;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * TODO 远程连接.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/20 20:44
 */

@Component
@FeignClient(name = "SOCKET-SERVER", fallback = SocketRemoteFallback.class)
public interface SocketRemote {

  /**
   * TODO 游戏通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  @RequestMapping(value = "/notice/gameInviteNotice", method = RequestMethod.POST)
  Result gameInviteNotice(@RequestBody Map<String, Object> params);
}
