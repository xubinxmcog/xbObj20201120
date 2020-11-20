package com.enuos.live.service.impl;

import com.enuos.live.mapper.GroupMessageFileMapper;
import com.enuos.live.pojo.GroupMessageFile;
import com.enuos.live.service.GroupMessageFileService;
import com.enuos.live.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/21 9:21
 */
@Slf4j
@Service("groupMessageFileService")
public class GroupMessageFileServiceImpl implements GroupMessageFileService {

  @Resource
  private GroupMessageFileMapper groupMessageFileMapper;

  @Override
  public void newGroupMessageFile(Map<String, Object> params) {
    Integer messageType = (Integer) params.get("messageType");
    Long recordId = ((Number) params.get("recordId")).longValue();
    Long groupId = ((Number) params.get("groupId")).longValue();
    GroupMessageFile groupMessageFile = new GroupMessageFile();
    groupMessageFile.setGroupId(groupId);
    groupMessageFile.setRecordId(recordId);
    switch (messageType) {
      case 2:
        groupMessageFile.setFileType(0);
        groupMessageFile.setFileWidth((Integer) params.get("fileWidth"));
        groupMessageFile.setFileHeight((Integer) params.get("fileHeight"));
        groupMessageFile.setFileUrl(StringUtils.nvl(params.get("fileUrl")));
        break;
      case 3:
        groupMessageFile.setFileType(1);
        groupMessageFile.setFileDuration((Integer) params.get("fileDuration"));
        groupMessageFile.setFileUrl(StringUtils.nvl(params.get("fileUrl")));
        break;
      default:
        groupMessageFile.setFileType(2);
        groupMessageFile.setFileWidth((Integer) params.get("fileWidth"));
        groupMessageFile.setFileHeight((Integer) params.get("fileHeight"));
        groupMessageFile.setFileDuration((Integer) params.get("fileDuration"));
        groupMessageFile.setFileUrl(StringUtils.nvl(params.get("fileUrl")));
        groupMessageFile.setFileCoverUrl(StringUtils.nvl(params.get("coverUrl")));
        break;
    }
    this.groupMessageFileMapper.newGroupMessageFile(groupMessageFile);
  }

  @Override
  public GroupMessageFile getMessageFileInfo(Long recordId) {
    return this.groupMessageFileMapper.getMessageFileInfo(recordId);
  }

  @Override
  public void deleteGroupMessage(Long groupId) {
    this.groupMessageFileMapper.deleteGroupMessage(groupId);
  }

  @Override
  public void deleteGroupVoiceMessage(Long groupId, Long userId) {

  }

  @Override
  public void deleteGroupVoiceMessageByList(Long groupId, List<Long> list) {

  }


}
