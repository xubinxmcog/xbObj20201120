package com.enuos.live.rest.fallback;

import com.enuos.live.pojo.FriendPO;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 错降级处理
 *
 * @author WangCaiWen Created on 2020/4/10 13:16
 */
@Slf4j
@Component
public class UserRemoteFallback implements UserRemote {

  @Override
  public Map<String, Object> getUserBase(Long userId, Long friendId) {
    return Collections.emptyMap();
  }

  @Override
  public Integer getRelation(Long userId, Long toUserId, Integer flag) {
    return null;
  }

  @Override
  public List<Map<String, Object>> getUserList(List<Long> userIdList) {
    return Collections.emptyList();
  }

  @Override
  public String getNickName(List<Long> userIdList) {
    return null;
  }

  @Override
  public Result openMakeFriend(FriendPO friendPO) {
    return null;
  }

  @Override
  public List<Long> getUserIdList(Integer isMember) {
    return Collections.emptyList();
  }

  @Override
  public List<Long> getUserIdList() {
    return Collections.emptyList();
  }

  @Override
  public Map<String, Object> getUserFrame(Long userId) {
    return Collections.emptyMap();
  }

  @Override
  public Result getBase(Long userId) {
    return null;
  }
}
