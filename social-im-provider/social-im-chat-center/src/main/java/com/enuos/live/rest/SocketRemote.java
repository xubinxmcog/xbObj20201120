package com.enuos.live.rest;

import com.enuos.live.rest.fallback.SocketRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * TODO 聊天服务.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/22 - 2020/7/28
 */
@Component
@FeignClient(name = "SOCKET-SERVER", fallback = SocketRemoteFallback.class)
public interface SocketRemote {

  /**
   * TODO 互动通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/notice/newInteractiveNotice")
  Result newInteractNotice(@RequestBody Map<String, Object> params);

  /**
   * TODO 软件通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/notice/newSystemNotice")
  Result newSystemNotice(@RequestBody Map<String, Object> params);

  /**
   * TODO 解散通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/notice/dissolveChatNotice")
  Result dissolveChatNotice(@RequestBody Map<String, Object> params);

  /**
   * TODO 添加通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/notice/newAddFriendNotice")
  Result newAddFriendNotice(@RequestBody Map<String, Object> params);

  /**
   * TODO 群聊通知.
   *
   * @param params [groupId, message]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:44
   * @update 2020/11/11 15:44
   */
  @PostMapping(value = "/notice/groupNoticeMessage")
  Result groupNoticeMessage(@RequestBody Map<String, Object> params);

}
