package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TODO 单聊信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/4/13 15:13
 */

@Data
public class ChatMessage implements Serializable {
  private static final long serialVersionUID = 7201605671559720812L;
  /** 主键ID. */
  private Long id;
  /** 用户ID. */
  private Long userId;
  /** 目标ID. */
  private Long targetId;
  /** 消息内容. */
  private String message;
  /** 消息唯一标识符. */
  private String messageId;
  /** 消息类型 [0 文本 1 Emoji 2 图片 3 语音 4 视频 5 游戏邀请 6 语音房邀请]. */
  private Integer messageType;
  /** 是否已读 [0 未读 1 已读]. */
  private Integer messageRead;
  /** 是否操作 [0 否 1 是] 用于标记音频文件. */
  private Integer messageAction;
  /** 表情地址. */
  private String messageEmojiUrl;
  /** 表情名称. */
  private String messageEmojiName;
  /** 创建时间. */
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
}
