package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 游戏房间(GameRoom)实体类
 *
 * @author WangCaiWen
 * @since 2020-05-19 10:42:37
 */
@Data
public class GameRoom implements Serializable {

  private static final long serialVersionUID = 352341005884944126L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 房间ID
   */
  private Long roomId;
  /**
   * 游戏编号
   */
  private Long gameCode;
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