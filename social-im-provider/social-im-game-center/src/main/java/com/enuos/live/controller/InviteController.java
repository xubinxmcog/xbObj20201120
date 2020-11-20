package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.InviteService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 游戏邀请(Invite)控制层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/20 20:20
 */

@RestController
@RequestMapping("/invite")
public class InviteController {

  @Resource
  private InviteService inviteService;

  /**
   * TODO 发送游戏邀请.
   *
   * @param params 参数
   * @return 发送结果
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  @Cipher
  @RequestMapping(value = "/sendGameInvite", method = RequestMethod.POST)
  public Result sendGameInvite(@RequestBody Map<String, Object> params) {
    return this.inviteService.sendGameInvite(params);
  }
}
