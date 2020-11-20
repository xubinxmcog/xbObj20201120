package com.enuos.live.rest.fallback;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/20 20:47
 */

@Component
public class SocketRemoteFallback implements SocketRemote {

  @Override
  public Result gameInviteNotice(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }
}
