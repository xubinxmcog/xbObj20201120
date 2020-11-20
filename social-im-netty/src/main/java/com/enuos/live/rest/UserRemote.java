package com.enuos.live.rest;

import com.enuos.live.rest.impl.UserRemoteFallback;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TODO 用户中心.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/4/14 - 2020/7/29
 */

@Component
@FeignClient(name = "SOCIAL-IM-USER", fallback = UserRemoteFallback.class)
public interface UserRemote {

  /**
   * TODO 用户基础信息.
   *
   * @param userId 用户ID
   * @param friendId 目标ID
   * @return 用户信息
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @GetMapping(value = "/user/open/getUserBase")
  Map<String, Object> getUserBase(@RequestParam("userId") Long userId, @RequestParam("friendId") Long friendId);

  /**
   * TODO 用户信息列表.
   *
   * @param userIdList 用户列表
   * @return 信息列表
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @PostMapping(value = "/user/open/getUserList")
  List<Map<String, Object>> getUserList(@RequestBody List<Long> userIdList);

  /**
   * TODO 用户详情.
   *
   * @param userId 用户ID
   * @return 用户详情
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @GetMapping(value = "/user/open/getUserMsg")
  Map<String, Object> getUserMsg(@RequestParam("userId") Long userId);

  /**
   * TODO 名称拼接.
   *
   * @param userIdList 用户列表
   * @return 拼接信息
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @PostMapping(value = "/user/open/getNickName")
  String getNickName(@RequestBody List<Long> userIdList);

  /**
   * TODO 获得用户关系.
   *
   * @param userId 用户ID
   * @param toUserId 目标ID
   * @param flag 标记[0：好友关系；1黑名单关系]
   * @return 用户关系[0；false；1：true]
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/user/open/getRelation")
  Integer getRelation(@RequestParam("userId") Long userId, @RequestParam("toUserId") Long toUserId,
      @RequestParam("flag") Integer flag);

  /**
   * TODO 用户列表.
   *
   * @param isMember 是否会员[0否1是]，不传则全部
   * @return 用户列表
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @GetMapping(value = "/user/open/getUserIdList")
  List<Long> getUserIdList(@RequestParam("isMember") Integer isMember);

  /**
   * TODO 获得装饰信息.
   *
   * @param userId 用户ID
   * @return 装饰信息
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  @GetMapping(value = "/user/open/getUserFrame")
  Map<String, Object> getUserFrame(@RequestParam("userId") Long userId);

  /**
   * TODO 任务达成.
   *
   * @param params 任务信息
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @PostMapping(value = "/task/handler")
  void taskHandler(@RequestBody Map<String, Object> params);

}
