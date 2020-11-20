package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TODO 群聊信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/4/13 15:14
 */

@Data
public class GroupMessage implements Serializable {
  private static final long serialVersionUID = 3705069259042990296L;
  /** 主键ID. */
  private Long id;
  /** 群聊ID. */
  private Long groupId;
  /** 发送者ID. */
  private Long sendId;
  /** 发送者昵称. */
  private String sendName;
  /** 消息内容. */
  private String message;
  /** 消息的唯一标识符. */
  private String messageId;
  /** 消息分类 [0 聊天 1 提示]. */
  private Integer messageSort;
  /** 聊天类型 [0 文本 1 Emoji 1 图片 2 语音 3 视频]. */
  private Integer messageType;
  /** 表情地址. */
  private String messageEmojiUrl;
  /** 表情名称. */
  private String messageEmojiName;
  /** 创建时间. */
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
}
