package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/24 13:39
 */
@Slf4j
@RestController
@RequestMapping("/notice")
public class NoticeController {

  @Resource
  private ChatService chatService;
  @Resource
  private GroupMemberService groupMemberService;
  @Resource
  private NoticeInteractService noticeInteractService;
  @Resource
  private NoticeMemberService noticeMemberService;

  private static final int PAGE_NUM = 1;
  private static final int PAGE_SIZE = 50;
  private static final int PAGE_SIZE_NOTICE = 10;

  private static final String STRING_PAGE_NUM = "pageNum";
  private static final String STRING_PAGE_SIZE = "pageSize";

  /**
   * 获得聊天列表
   */
  @Cipher
  @RequestMapping(value = "/getNoticeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result getChatNoticeList(@RequestBody Map<String, Object> params) {
    // 设置默认页数
    if (!params.containsKey(STRING_PAGE_NUM)) {
      params.put(STRING_PAGE_NUM, PAGE_NUM);
    }
    // 设置默认条数
    if (!params.containsKey(STRING_PAGE_SIZE)) {
      params.put(STRING_PAGE_SIZE, PAGE_SIZE);
    }
    return this.chatService.getChatNoticeList(params);
  }

  /**
   * 获得互动通知列表
   */
  @Cipher
  @RequestMapping(value = "/getInteractNoticeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result getInteractNoticeList(@RequestBody Map<String, Object> params) {
    // 设置默认页数
    if (!params.containsKey(STRING_PAGE_NUM)) {
      params.put(STRING_PAGE_NUM, PAGE_NUM);
    }
    // 设置默认条数
    if (!params.containsKey(STRING_PAGE_SIZE)) {
      params.put(STRING_PAGE_SIZE, PAGE_SIZE_NOTICE);
    }
    return this.noticeInteractService.getInteractNoticeList(params);
  }

  /**
   * 根据通知ID - 删除通知
   */
  @Cipher
  @RequestMapping(value = "/deleteNotice", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result deleteNotice(@RequestBody Map<String, Object> params) {
    // 操作 0 系统 1 互动
    Integer noticeAction = (Integer) params.get("noticeAction");
    if (noticeAction == 0) {
      return this.noticeMemberService.deleteMemberNotice(params);
    }
    return this.noticeInteractService.deleteInteractNotice(params);
  }

  /**
   * 获得软件通知列表
   */
  @Cipher
  @RequestMapping(value = "/getSystemNoticeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result getSystemNoticeList(@RequestBody Map<String, Object> params) {
    // 设置默认页数
    if (!params.containsKey(STRING_PAGE_NUM)) {
      params.put(STRING_PAGE_NUM, PAGE_NUM);
    }
    // 设置默认条数
    if (!params.containsKey(STRING_PAGE_SIZE)) {
      params.put(STRING_PAGE_SIZE, PAGE_SIZE_NOTICE);
    }
    return this.noticeMemberService.getMemberNoticeList(params);
  }

  /**
   * 获得通知详情
   */
  @Cipher
  @PostMapping(value = "/getSystemNoticeInfo", produces = "application/json;charset=UTF-8")
  public Result getSystemNoticeInfo(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.getNoticeInfo(params);
  }

  /**
   * 更新聊天设置 置顶、删除、免打扰
   */
  @Cipher
  @RequestMapping(value = "/updateNoticeSetting", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result updateNoticeSetting(@RequestBody Map<String, Object> params) {
    // 操作 1 单聊 2 群聊
    Integer chatAction = (Integer) params.get("chatAction");
    if (chatAction == 1) {
      return this.chatService.updUserChatSetting(params);
    } else {
      return this.groupMemberService.updUserGroupSetting(params);
    }
  }

}
