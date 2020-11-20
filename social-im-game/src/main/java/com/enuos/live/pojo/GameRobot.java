package com.enuos.live.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * TODO 机器人.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/9/17 21:07
 */

@Data
public class GameRobot implements Serializable {
  private static final long serialVersionUID = -77669285242590839L;
  /** 机器人ID. */
  private Long robotId;
  /** 机器人性别 [1 男 2女]. */
  private Integer robotSex;
  /** 机器人名称. */
  private String robotName;
  /** 机器人头像. */
  private String robotAvatar;
}