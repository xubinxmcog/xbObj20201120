package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户接收语音消息操作(GroupMessageVoice)实体类.
 *
 * @author WangCaiWen
 * @since 2020-05-13 15:31:32
 */
@Data
public class GroupMessageVoice implements Serializable {

  private static final long serialVersionUID = -39063056803731758L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 群聊ID
   */
  private Long groupId;
  /**
   * 记录ID
   */
  private Long recordId;
  /**
   * 用户ID
   */
  private Long userId;
  /**
   * 是否操作 [0 否 1 是] 用于标记音频文件
   */
  private Object action;
}