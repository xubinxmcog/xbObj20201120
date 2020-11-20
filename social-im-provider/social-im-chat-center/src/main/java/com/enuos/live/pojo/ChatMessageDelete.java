package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 聊天标记(ChatMessageDelete)实体类
 *
 * @author WangCaiWen
 * @since 2020-05-12 16:16:15
 */
@Data
public class ChatMessageDelete implements Serializable {

  private static final long serialVersionUID = -10304850677786402L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 用户ID
   */
  private Long userId;
  /**
   * 目标ID
   */
  private Long targetId;
  /**
   * 标记删除
   */
  private Integer signNum;
  /**
   * 创建时间
   */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;
  /**
   * 更新时间
   */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;

}