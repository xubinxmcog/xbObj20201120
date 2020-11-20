package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.ChatMessageDeleteService;
import com.enuos.live.service.ChatMessageService;
import com.enuos.live.service.ChatService;
import com.enuos.live.service.GroupMessageDeleteService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/9 16:39
 */

@RestController
@RequestMapping("/chat")
public class ChatController {

  @Resource
  private ChatService chatService;
  @Resource
  private ChatMessageService chatMessageService;
  @Resource
  private ChatMessageDeleteService chatMessageDeleteService;
  @Resource
  private GroupMessageDeleteService groupMessageDeleteService;

  private static final int PAGE_NUM = 1;
  private static final int PAGE_SIZE = 10;

  private static final String STRING_PAGE_NUM = "pageNum";
  private static final String STRING_PAGE_SIZE = "pageSize";

  /**
   * 获得聊天记录
   */
  @Cipher
  @RequestMapping(value = "/getChatMessage", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result getChatMessage(@RequestBody Map<String, Object> params) {
    // 设置默认页数
    if (!params.containsKey(STRING_PAGE_NUM)) {
      params.put(STRING_PAGE_NUM, PAGE_NUM);
    }
    // 设置默认条数
    if (!params.containsKey(STRING_PAGE_SIZE)) {
      params.put(STRING_PAGE_SIZE, PAGE_SIZE);
    }
    return this.chatMessageService.getChatMessage(params);
  }

  /**
   * 获得聊天设置
   */
  @Cipher
  @RequestMapping(value = "/getUserChatSetting", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result getUserChatSetting(@RequestBody Map<String, Object> params) {
    return this.chatService.getUserChatSetting(params);
  }

  /**
   * 清空聊天记录
   */
  @Cipher
  @RequestMapping(value = "/emptyChatMessage", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public Result emptyChatMessage(@RequestBody Map<String, Object> params) {
    Integer sortMessage = (Integer) params.get("sort");
    if (sortMessage != 1) {
      return this.groupMessageDeleteService.updateSignNum(params);
    } else {
      return this.chatMessageDeleteService.updateSignNum(params);
    }
  }

}
