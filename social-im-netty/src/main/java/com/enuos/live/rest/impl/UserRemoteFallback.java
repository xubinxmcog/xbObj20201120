package com.enuos.live.rest.impl;

import com.enuos.live.rest.UserRemote;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/14 - 2020/7/28
 */

@Component
public class UserRemoteFallback implements UserRemote {

  @Override
  public Map<String, Object> getUserBase(Long userId, Long friendId) {
    return null;
  }

  @Override
  public List<Map<String, Object>> getUserList(List<Long> userIdList) {
    return null;
  }

  @Override
  public Map<String, Object> getUserMsg(Long userId) {
    return null;
  }

  @Override
  public String getNickName(List<Long> userIdList) {
    return null;
  }

  @Override
  public Integer getRelation(Long userId, Long toUserId, Integer flag) {
    return null;
  }

  @Override
  public List<Long> getUserIdList(Integer isMember) {
    return null;
  }

  @Override
  public Map<String, Object> getUserFrame(Long userId) {
    return null;
  }

  @Override
  public void taskHandler(Map<String, Object> params) { }
}
