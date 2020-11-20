package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;

/**
 * TODO 时间记录.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020-11-05 15:16:12
 */

@Data
public class GamePlayTime implements Serializable {
  private static final long serialVersionUID = -59964450254407810L;
  /** 主键ID */
  private Integer id;
  /** 用户ID */
  private Long userId;
  /** 累计时间(s) */
  private Long totalTime;
  /** 创建时间 */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
  /** 更新时间 */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updateTime;
}