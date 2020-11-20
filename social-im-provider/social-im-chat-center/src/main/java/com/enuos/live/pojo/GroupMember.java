package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author WangCaiWen Created on 2020/4/13 15:14
 */
@Data
public class GroupMember implements Serializable {

  private static final long serialVersionUID = 5526753202766064689L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 群聊ID
   */
  private Long groupId;
  /**
   * 用户ID
   */
  private Long userId;
  /**
   * 标记置顶 [0否 1是]
   */
  private Integer flagTop;
  /**
   * 标记移除 [0否 1是]
   */
  private Integer flagDelete;
  /**
   * 标记权限 [0普通 1管理 2房主]
   */
  private Integer flagAuthority;
  /**
   * 聊天状态 [0闲置中 1聊天中]
   */
  private Integer chatStatus;
  /**
   * 免打扰 [0关闭 1开启]
   */
  private Integer notDisturb;
  /**
   * 未读数量
   */
  private Integer unreadNum;
  /**
   * 消息记录ID.
   */
  private Long messageId;
  /**
   * 消息时间
   */
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime messageTime;
  /**
   * 创建时间
   */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
  /**
   * 更新时间
   */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updateTime;
}
