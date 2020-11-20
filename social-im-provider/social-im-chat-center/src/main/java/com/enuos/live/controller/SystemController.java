package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.NoticeMemberService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * TODO 系统通知.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/9 13:07
 */

@RestController
@RequestMapping("/system")
public class SystemController {

  @Resource
  private NoticeMemberService noticeMemberService;

  /**
   * TODO 系统通知.
   *
   * @param params [通知信息]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 9:53
   * @update 2020/11/13 9:53
   */
  @Cipher
  @PostMapping(value = "/newsNotice", produces = "application/json;charset=UTF-8")
  public Result sendUserNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.saveMemberNoticeList(params);
  }

  /**
   * TODO 歌曲审核.
   *
   * @param params [receiveId, songName, auditResult]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 9:30
   * @update 2020/11/13 9:30
   */
  @Cipher
  @PostMapping(value = "/songAuditNotice", produces = "application/json;charset=UTF-8")
  public Result songAuditNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.songAuditNotice(params);
  }

  /**
   * TODO 题目审核.
   *
   * @param params [receiveId, topicType, auditResult, coinReward]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:01
   * @update 2020/11/13 10:01
   */
  @Cipher
  @PostMapping(value = "/topicAuditNotice", produces = "application/json;charset=UTF-8")
  public Result topicAuditNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.topicAuditNotice(params);
  }

  /**
   * TODO 信息处理.
   *
   * @param params [receiveId, handleType, targetTime]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:07
   * @update 2020/11/13 10:07
   */
  @Cipher
  @PostMapping(value = "/infoAuditNotice", produces = "application/json;charset=UTF-8")
  public Result infoAuditNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.infoAuditNotice(params);
  }

  /**
   * TODO 举报反馈.
   *
   * @param params [receiveId, reportUser]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:28
   * @update 2020/11/13 10:28
   */
  @Cipher
  @PostMapping(value = "/memberAuditNotice", produces = "application/json;charset=UTF-8")
  public Result memberAuditNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.memberAuditNotice(params);
  }

  /**
   * TODO 处罚信息.
   *
   * @param params [receiveId, handleType, bannedDays, handleMessage]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:31
   * @update 2020/11/13 10:31
   */
  @Cipher
  @PostMapping(value = "/punishmentNotice", produces = "application/json;charset=UTF-8")
  public Result punishmentNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.punishmentNotice(params);
  }

}
