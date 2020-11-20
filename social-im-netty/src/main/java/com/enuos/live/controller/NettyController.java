package com.enuos.live.controller;

import com.enuos.live.handler.ChatHandler;
import com.enuos.live.handler.VoiceHandler;
import com.enuos.live.handler.WarnHandler;
import com.enuos.live.rest.RoomRemote;
import com.enuos.live.result.Result;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * TODO 通知调用(notice)控制层.
 *
 * @author WangCaiWen
 * @version 1.0
 * @since 2020-04-09 11:26:13
 */

@Slf4j
@RestController
@RequestMapping("/notice")
public class NettyController {

  @Resource
  private WarnHandler warnHandler;
  @Resource
  private ChatHandler chatHandler;
  @Resource
  private VoiceHandler voiceHandler;

  /**
   * TODO 互动通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @since 2020/4/9 - 2020/4/9
   */
  @PostMapping(value = "/newInteractiveNotice")
  public Result newInteractNotice(@RequestBody Map<String, Object> params) {
    return this.warnHandler.interactNotice(params);
  }

  /**
   * TODO 软件通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @since 2020/4/9 - 2020/4/9
   */
  @PostMapping(value = "/newSystemNotice")
  public Result newSystemNotice(@RequestBody Map<String, Object> params) {
    return this.warnHandler.softwareNotice(params);
  }

  /**
   * TODO 群聊通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @since 2020/4/9 - 2020/4/9
   */
  @RequestMapping(value = "/dissolveChatNotice", method = RequestMethod.POST)
  public Result dissolveChatNotice(@RequestBody Map<String, Object> params) {
    return this.warnHandler.dissolveChatNotice(params);
  }

  /**
   * TODO 游戏通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  @PostMapping(value = "/gameInviteNotice")
  public Result gameInviteNotice(@RequestBody Map<String, Object> params) {
    return this.chatHandler.sendInvite(params);
  }

  /**
   * TODO 添加通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/newAddFriendNotice")
  public Result newAddFriendNotice(@RequestBody Map<String, Object> params) {
    return this.warnHandler.newAddFriendNotice(params);
  }

  /**
   * TODO 群聊通知.
   *
   * @param params [groupId, message]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:44
   * @update 2020/11/11 15:44
   */
  @PostMapping(value = "/groupNoticeMessage")
  public Result groupNoticeMessage(@RequestBody Map<String, Object> params) {
    return this.warnHandler.groupNoticeMessage(params);
  }

  @Resource
  private RoomRemote roomRemote;

  /**
   * 解散群聊通知
   *
   * @return 调用结果
   */
  @RequestMapping(value = "/isBanned", method = RequestMethod.GET)
  public Object isBanned(Long roomId, Long userId) {
    return roomRemote.isBanned(roomId, userId);
  }


  /**
   * @MethodName: giveGiftNotice
   * @Description: TODO 赠送礼物通知
   * @Param: [params]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 10:33 2020/8/25
   **/
  @PostMapping("/giveGiftNotice")
  public Result giveGiftNotice(@RequestBody Map<String, Object> params) {
    return voiceHandler.giveGiftNotice(params);
  }

  /**
   * @MethodName: vipGradeNotice
   * @Description: TODO 会员升级通知
   * @Param: [params]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 15:23 2020/8/25
   **/
  @PostMapping("/vipGradeNotice")
  public Result vipGradeNotice(@RequestBody Map<String, Object> params) {
    return warnHandler.vipGradeNotice(params);
  }

  /**
   * @MethodName: exceptionEndBroadcast
   * @Description: TODO 异常下播
   * @Param: [roomId, userId]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 15:19 2020/9/24
   **/
  @GetMapping("/exceptionEndBroadcast")
  public Result exceptionEndBroadcast(Long roomId, Long userId) {

    return voiceHandler.exceptionEndBroadcast(roomId, userId);

  }

  // 文件大小
  @Value("${room.fileSize}")
  private double fileSize;

  @GetMapping("/test")
  public Object test() {
    System.out.println(fileSize);
    return fileSize;
  }

}
