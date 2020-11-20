package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.GroupMemberService;
import com.enuos.live.service.GroupMessageService;
import com.enuos.live.service.GroupService;
import java.util.Map;
import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 群聊控制层.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version 2.0
 * @since 2020/4/9 16:53
 */

@RestController
@RequestMapping("/group")
public class GroupController {

  @Resource
  private GroupService groupService;
  @Resource
  private GroupMemberService groupMemberService;
  @Resource
  private GroupMessageService groupMessageService;

  private static final int DEFAULT_PAGE_NUM = 1;
  private static final int DEFAULT_PAGE_SIZE = 10;

  private static final String PARAMS_PAGE_NUM = "pageNum";
  private static final String PARAMS_PAGE_SIZE = "pageSize";

  /**
   * TODO 聊天记录.
   *
   * @param params 页码信息等
   * @return list 聊天记录
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/26 7:17
   * @update 2020/8/14 9:05
   */
  @Cipher
  @PostMapping(value = "/getGroupMessage", produces = "application/json;charset=UTF-8")
  public Result getGroupMessage(@RequestBody Map<String, Object> params) {
    if (!params.containsKey(PARAMS_PAGE_NUM)) {
      params.put(PARAMS_PAGE_NUM, DEFAULT_PAGE_NUM);
    }
    if (!params.containsKey(PARAMS_PAGE_SIZE)) {
      params.put(PARAMS_PAGE_SIZE, DEFAULT_PAGE_SIZE);
    }
    return this.groupMessageService.getGroupMessage(params);
  }

  /**
   * TODO 创建群聊.
   *
   * @param params 群聊参数
   * @return 创建结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:11
   */
  @Cipher
  @PostMapping(value = "/newGroupChat", produces = "application/json;charset=UTF-8")
  public Result newGroupChat(@RequestBody Map<String, Object> params) {
    return this.groupService.newGroupChat(params);
  }

  /**
   * TODO 群聊列表.
   *
   * @param params [userId]
   * @return list 群聊列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:13
   */
  @Cipher
  @PostMapping(value = "/getUserGroupList", produces = "application/json;charset=UTF-8")
  public Result getUserTheGroupListByUserId(@RequestBody Map<String, Object> params) {
    return this.groupService.getUserTheGroupListByUserId(params);
  }

  /**
   * TODO 邀请加入.
   *
   * @param params 邀请信息
   * @return 邀请结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:15
   */
  @Cipher
  @PostMapping(value = "/inviteJoinGroup", produces = "application/json;charset=UTF-8")
  public Result inviteJoinGroup(@RequestBody Map<String, Object> params) {
    return this.groupService.inviteJoinGroup(params);
  }

  /**
   * TODO 搜索群名.
   *
   * @param params 搜索信息
   * @return 搜索结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:28
   */
  @Cipher
  @PostMapping(value = "/searchGroupByName", produces = "application/json;charset=UTF-8")
  public Result searchGroupByName(@RequestBody Map<String, Object> params) {
    return this.groupService.searchGroupByName(params);
  }

  /**
   * TODO 聊天设置.
   *
   * @param params 用户信息
   * @return 聊天设置
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/12 10:03
   */
  @Cipher
  @PostMapping(value = "/getUserGroupSetting", produces = "application/json;charset=UTF-8")
  public Result getUserGroupSetting(@RequestBody Map<String, Object> params) {
    return this.groupService.getUserGroupSetting(params);
  }

  /**
   * TODO 离开群聊.
   * 群主离开，无移交房主权限，解散群聊
   *
   * @param params 用户信息
   * @return 离开结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/21 18:18
   * @update 2020/8/14 9:31
   */
  @Cipher
  @PostMapping(value = "/leaveGroupChat", produces = "application/json;charset=UTF-8")
  public Result leaveGroupChat(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.leaveGroupChat(params);
  }

  /**
   * TODO 更新信息.
   *
   * @param params 群聊信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/21 18:18
   * @update 2020/8/14 9:33
   */
  @Cipher
  @PostMapping(value = "/updGroupInfo", produces = "application/json;charset=UTF-8")
  public Result updGroupNameOrNoticeAndIntro(@RequestBody Map<String, Object> params) {
    return this.groupService.updGroupNameOrNoticeAndIntro(params);
  }

  /**
   * TODO 管理列表.
   * 50-3/100-6
   *
   * @param params [groupId]
   * @return list 管理列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:34
   */
  @Cipher
  @PostMapping(value = "/getGroupAdminList", produces = "application/json;charset=UTF-8")
  public Result getGroupAdminList(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.getGroupAdminList(params);
  }

  /**
   * TODO 成员列表.
   *
   * @param params [groupId]
   * @return list 成员列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:37
   */
  @Cipher
  @PostMapping(value = "/getGroupUserList", produces = "application/json;charset=UTF-8")
  public Result getGroupUserList(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.getGroupUserList(params);
  }

  /**
   * TODO 设置管理.
   *
   * @param params 设置信息
   * @return 设置结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:38
   */
  @Cipher
  @PostMapping(value = "/updateGroupAdmin", produces = "application/json;charset=UTF-8")
  public Result updateGroupAdmin(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.updateGroupAdmin(params);
  }

  /**
   * TODO 移交权限.
   *
   * @param params 用户信息
   * @return 移交结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/14 9:40
   */
  @Cipher
  @PostMapping(value = "/handOverGroupAdmin", produces = "application/json;charset=UTF-8")
  public Result handOverGroupAdminLimit(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.handOverGroupAdminLimit(params);
  }

  /**
   * TODO 删除成员.
   *
   * @param params 成员信息
   * @return 删除结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/22 18:16
   * @update 2020/8/14 9:42
   */
  @Cipher
  @PostMapping(value = "/deleteGroupUser", produces = "application/json;charset=UTF-8")
  public Result deleteGroupUser(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.deleteGroupUser(params);
  }

  /**
   * TODO 提升等级.
   *
   * @param params 用户信息
   * @return 提升结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/14 9:49
   * @update 2020/8/14 9:49
   */
  @Cipher
  @PostMapping(value = "/upgradeGroupGrade", produces = "application/json;charset=UTF-8")
  public Result upgradeGroupGrade(@RequestBody Map<String, Object> params) {
    return this.groupService.upgradeGroupGrade(params);
  }

}
