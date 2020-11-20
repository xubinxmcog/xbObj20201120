package com.enuos.live.service.impl;

import com.enuos.live.mapper.GroupMessageVoiceMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.GroupMessageVoiceService;
import com.enuos.live.utils.JsonUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 用户接收语音消息操作(GroupMessageVoice)表服务实现类.
 *
 * @author WangCaiWen
 * @since 2020-05-13 15:31:32
 */
@Service("groupMessageVoiceService")
public class GroupMessageVoiceServiceImpl implements GroupMessageVoiceService {

  @Resource
  private GroupMessageVoiceMapper groupMessageVoiceMapper;

  @Override
  public Result insertMessageUnread(Map<String, Object> params) {
    Long recordId = ((Number) params.get("recordId")).longValue();
    Long groupId = ((Number) params.get("groupId")).longValue();
    List<Long> userIdList = JsonUtils.toListType(params.get("userList"), Long.class);
    if (userIdList != null) {
      List<Map<String, Object>> voiceMessageList = new LinkedList<>();
      Map<String, Object> result;
      for (Long aLong : userIdList) {
        result = new HashMap<>(16);
        result.put("groupId", groupId);
        result.put("recordId", recordId);
        result.put("userId", aLong);
        voiceMessageList.add(result);
      }
      this.groupMessageVoiceMapper.saveUserMessageList(voiceMessageList);
    }
    return Result.success();
  }

  @Override
  public Result listenToVoiceGroup(Map<String, Object> params) {
    long recordId = ((Number) params.get("recordId")).longValue();
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (recordId <= 0 || groupId <= 0 || userId <= 0) {
      return Result.error();
    }
    this.groupMessageVoiceMapper.updateMessageVoice(params);
    return Result.success();
  }

  @Override
  public void deleteGroupMessage(Long groupId) {
    this.groupMessageVoiceMapper.deleteGroupMessage(groupId);
  }

  @Override
  public void deleteGroupVoiceMessage(Long groupId, Long userId) {
    this.groupMessageVoiceMapper.deleteGroupVoiceMessage(groupId, userId);
  }

  @Override
  public void deleteGroupVoiceMessageByList(Long groupId, List<Long> list) {
    this.groupMessageVoiceMapper.deleteGroupVoiceMessageByList(groupId, list);
  }
}