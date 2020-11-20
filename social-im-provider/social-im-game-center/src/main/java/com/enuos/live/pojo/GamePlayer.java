package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * (GamePlayer)实体类
 *
 * @author WangCaiWen
 * @since 2020-05-19 10:37:52
 */
@Data
public class GamePlayer implements Serializable {

  private static final long serialVersionUID = -47764329679428623L;
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
   * 用户ID
   */
  private Long userId;
}