package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TODO 七乐通知.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/4/27 13:19
 */

@Data
public class NoticeMember implements Serializable {
  private static final long serialVersionUID = -4246027871868960623L;
  /** 主键ID. */
  private Long id;
  /** 用户ID. */
  private Long userId;
  /** 房间ID. */
  private Long roomId;
  /** 通知类型 0-审核 1-系统 2-会员. */
  private Integer noticeType;
  /** 阅读状态 0-未读 1-已读. */
  private Integer noticeRead;
  /** 通知编号. */
  private Long contentNo;
  /** 通知标题. */
  private String contentTitle;
  /** 通知描述. */
  private String contentIntro;
  /** 通知类型 0-普通 1-跳转. */
  private Integer contentType;
  /** 通知来源 0-系统 1-管理. */
  private Integer contentSource;
  /** 创建人员工号. */
  private Integer adminId;
  /** 创建人员姓名. */
  private String adminName;
  /** 创建时间. */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
  /** 更新时间. */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updateTime;
}
