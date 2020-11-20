package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 用户游玩记录(GameUserRecord)实体类
 *
 * @author WangCaiWen
 * @since 2020-05-19 10:45:13
 */
@Data
public class GameUserRecord implements Serializable {

  private static final long serialVersionUID = 830495421121962597L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 用户ID
   */
  private Long userId;
  /**
   * 游戏次数
   */
  private Integer gamePlay;
  /**
   * 游戏编号
   */
  private Long gameCode;
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