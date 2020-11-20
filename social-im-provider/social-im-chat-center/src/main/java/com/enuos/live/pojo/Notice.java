package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author WangCaiWen Created on 2020/4/29 10:15
 */
@Data
public class Notice implements Serializable {

  private static final long serialVersionUID = 7132972857434471035L;
  /**
   * 主键ID
   */
  private Integer id;
  /**
   * 发布编号
   */
  private Long publishNo;
  /**
   * 发布标题
   */
  private String publishTitle;
  /**
   * 发布状态 [0 未发布 1已发布 2 已撤回]
   */
  private Integer publishStatus;
  /**
   * 推送类型 [0全局 1会员]
   */
  private Integer publishType;
  /**
   * 推送介绍
   */
  private String publishIntro;
  /**
   * 发布内容
   */
  private String publishContent;
  /**
   * 创建人员工号
   */
  private Integer createAdminId;
  /**
   * 创建人员姓名
   */
  private String createAdminName;
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
