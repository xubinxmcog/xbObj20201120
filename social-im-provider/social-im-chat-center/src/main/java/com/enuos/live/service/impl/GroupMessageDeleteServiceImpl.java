package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.GroupMessageDeleteMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.GroupMemberService;
import com.enuos.live.service.GroupMessageDeleteService;
import com.enuos.live.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 聊天标记(GroupMessageDelete)表服务实现类
 *
 * @author WangCaiWen
 * @since 2020-05-12 16:16:54
 */
@Service("groupMessageDeleteService")
public class GroupMessageDeleteServiceImpl implements GroupMessageDeleteService {

  @Resource
  private GroupMessageDeleteMapper groupMessageDeleteMapper;
  @Resource
  private GroupMemberService groupMemberService;

  @Override
  public void insertMessage(List<Map<String, Object>> list) {
    this.groupMessageDeleteMapper.insertMessage(list);
  }

  @Override
  public Result updateSignNum(Map<String, Object> params) {
    Long groupId = ((Number) params.get("groupId")).longValue();
    Long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    this.groupMessageDeleteMapper.updateSignNum(groupId, userId);
    // 清空聊天内容
    this.groupMemberService.emptyResidualMessage(groupId, userId);
    return Result.success();
  }

  @Override
  public String queryMessageDateTime(Long groupId, Long userId) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String indexTime = this.groupMessageDeleteMapper.queryMessageDateTime(groupId, userId);
    if (StringUtils.isNotEmpty(indexTime)) {
      Timestamp time = Timestamp.valueOf(indexTime);
      return dtf.format(time.toLocalDateTime());
    }
    return null;
  }

  @Override
  public void deleteMessage(Long groupId, Long userId) {
    this.groupMessageDeleteMapper.deleteMessage(groupId, userId);
  }

  @Override
  public void deleteGroupMessage(Long groupId) {
    this.groupMessageDeleteMapper.deleteGroupMessage(groupId);
  }

  @Override
  public void deleteGroupMessageList(Long groupId, List<Long> list) {
    this.groupMessageDeleteMapper.deleteGroupMessageList(groupId, list);
  }
}