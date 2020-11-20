package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 聊天标记(GroupMessageDelete)实体类
 *
 * @author WangCaiWen
 * @since 2020-05-12 16:16:54
 */
@Data
public class GroupMessageDelete implements Serializable {

  private static final long serialVersionUID = -62927023590253526L;
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