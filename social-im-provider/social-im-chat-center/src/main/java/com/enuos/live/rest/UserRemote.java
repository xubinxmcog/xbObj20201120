package com.enuos.live.rest;

import com.enuos.live.pojo.FriendPO;
import com.enuos.live.rest.fallback.UserRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author wangcaiwen
 * Created on 2020/4/10 13:15
 */
@Component
@FeignClient(name = "SOCIAL-IM-USER", fallback = UserRemoteFallback.class)
public interface UserRemote {

  /**
   * TODO  获得用户信息.
   *
   * @param userId 玩家ID
   * @param friendId 好友ID
   * @return 用户信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/10 13:15
   * @update 2020/8/11 19:11
   */
  @GetMapping(value = "/user/open/getUserBase")
  Map<String, Object> getUserBase(@RequestParam("userId") Long userId, @RequestParam("friendId") Long friendId);

  /**
   * TODO 获得用户关系.
   *
   * @param userId 用户ID
   * @param toUserId 目标ID
   * @param flag 标记[0：好友关系；1黑名单关系]
   * @return 用户关系[0；false；1：true]
   * @author WangCaiWen
   * @date 2020/4/10 13:15
   * @update 2020/7/28 19:11
   */
  @GetMapping(value = "/user/open/getRelation")
  Integer getRelation(@RequestParam("userId") Long userId, @RequestParam("toUserId") Long toUserId, @RequestParam("flag") Integer flag);

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
   * 添加好友
   *
   * @param friendPO  添加信息
   * @return 添加结果
   */
  @RequestMapping(value = "/friend/open/makeFriend", method = RequestMethod.POST)
  Result openMakeFriend(@RequestBody FriendPO friendPO);

  /**
   * [OPEN]获取用户ID，会员或者非会员或全部
   *
   * @param isMember 是否会员[0否1是]，不传则全部   number类型
   * @return list
   */
  @RequestMapping(value = "/user/open/getUserIdList", method = RequestMethod.GET)
  List<Long> getUserIdList(@RequestParam("isMember") Integer isMember);

  /**
   * [OPEN]获取用户ID，会员或者非会员或全部
   *
   * @return list
   */
  @RequestMapping(value = "/user/open/getUserIdList", method = RequestMethod.GET)
  List<Long> getUserIdList();

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
   * TODO 用户信息.
   *
   * @param userId 用户ID
   * @return 用户信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/14 12:33
   * @update 2020/8/14 12:33
   */
  @GetMapping(value = "/user/open/getBaseForServer")
  Result getBase(@RequestParam("userId") Long userId);
}
