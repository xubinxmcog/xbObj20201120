package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;
import java.io.Serializable;

/**
 * @author WangCaiWen
 * Created on 2020/4/23 15:36
 */

@Data
public class ChatMessageInvite implements Serializable {
  private static final long serialVersionUID = -3438743954210562914L;
  /** 主键ID. */
  private Long id;
  /** 记录ID. */
  private Long recordId;
  /** 游戏ID. */
  private Long gameId;
  /** 房间ID. */
  private Long roomId;
  /** 邀请标题. */
  private String inviteTitle;
  /** 邀请图片. */
  private String inviteImage;
  /** 邀请状态 0-正常 1-取消. */
  private Integer inviteStatus;
  /** 接受状态 0-未接受 1-已接受. */
  private Integer acceptStatus;
  /** 创建时间. */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
}
