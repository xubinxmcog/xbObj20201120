package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.ChatMessageDelete;
import com.enuos.live.mapper.ChatMessageDeleteMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.ChatMessageDeleteService;
import com.enuos.live.service.ChatService;
import com.enuos.live.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * TODO 聊天删除服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-05-12 - 2020-07-28
 */

@Service("chatMessageDeleteService")
public class ChatMessageDeleteServiceImpl implements ChatMessageDeleteService {

  @Resource
  private ChatMessageDeleteMapper chatMessageDeleteMapper;
  @Resource
  private ChatService chatService;

  /**
   * TODO 新增数据.
   *
   * @param list 用户列表
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void insertMessage(List<Map<String, Object>> list) {
    this.chatMessageDeleteMapper.insertMessage(list);
  }

  /**
   * TODO 新增数据.
   *
   * @param chatMessageDelete 新增信息
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void insertChatMessageRelation(ChatMessageDelete chatMessageDelete) {
    this.chatMessageDeleteMapper.insertChatMessageRelation(chatMessageDelete);
  }

  /**
   * TODO 更新标记.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result updateSignNum(Map<String, Object> params) {
    Long userId = ((Number) params.get("userId")).longValue();
    Long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    this.chatMessageDeleteMapper.updateSignNum(userId, targetId);
    this.chatService.emptyResidualMessage(userId, targetId);
    return Result.success();
  }

  /**
   * TODO 标记时间.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 标记时间
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public String queryMessageDateTime(Long userId, Long targetId) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String indexTime = this.chatMessageDeleteMapper.queryMessageDateTime(userId, targetId);
    if (StringUtils.isNotEmpty(indexTime)) {
      Timestamp time = Timestamp.valueOf(indexTime);
      return dtf.format(time.toLocalDateTime());
    }
    return null;
  }

  /**
   * TODO 删除数据,
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void deleteMessage(Long userId, Long targetId) {
    this.chatMessageDeleteMapper.deleteMessage(userId, targetId);
  }

  /**
   * TODO 移除所有.
   *
   * @param userId 用户ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void deleteMessageAll(Long userId) {
    this.chatMessageDeleteMapper.deleteMessageAll(userId);
  }
}