package com.enuos.live.rest;

import com.enuos.live.pojo.Currency;
import com.enuos.live.rest.fallback.UserRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

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
   * @MethodName: getCurrency
   * @Description: TODO 获取钻石金币
   * @Param: [userId]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 14:12 2020/9/1
  **/
  @GetMapping("/user/open/getCurrencyForServer")
  Result getCurrency(@RequestParam("userId") Long userId);

  /**
   * @Description: 添加成长值
   * @Param: [userId, growth]
   * @Return: com.enuos.live.result.Result
   * @Author: wangyingjie
   * @Date: 2020/7/17
   */
  @GetMapping("/member/addGrowth")
  Result addGrowth(@RequestParam("userId") Long userId, @RequestParam("growth") Integer growth);

  /**
   * @Description: 金币钻石加减
   * @Param: [currency]
   * @Return: com.enuos.live.result.Result
   * @Author: wangyingjie
   * @Date: 2020/9/11
   */
  @PostMapping("/currency/upUserCurrency")
  Result upUserCurrency(@RequestBody Currency currency);


}
