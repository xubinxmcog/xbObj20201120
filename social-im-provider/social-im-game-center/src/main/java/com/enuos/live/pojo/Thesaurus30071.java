package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;
import lombok.Data;

/**
 * TODO 一站到底词库(Thesaurus)实体类
 *
 * @author wangcaiwen
 * @version 2.0
 * @since 2020-07-16 15:20:33
 */

@Data
public class Thesaurus30071 implements Serializable {

  private static final long serialVersionUID = 606235382485113885L;
  /**
   * 主键ID
   */
  private Integer id;
  /**
   * 题目
   */
  private String title;
  /**
   * 正确答案
   */
  private String answer;
  /**
   * 错误答案
   */
  private String wrongA;
  /**
   * 错误答案
   */
  private String wrongB;
  /**
   * 错误答案
   */
  private String wrongC;
  /**
   * 激活状态【0 未激活  1 已激活】
   */
  private Integer titleStatus;
  /**
   * 标签ID
   */
  private Integer labelId;
  /**
   * 创建人员工号
   */
  private Integer adminId;
  /**
   * 创建人员姓名
   */
  private String adminName;
  /**
   * 审核人员ID
   */
  private Integer auditId;
  /**
   * 审核人员姓名
   */
  private String auditName;
  /**
   * 激活状态【0 未审核  1 已通过  2 未通过 】
   */
  private Integer auditStatus;
  /**
   * 提交题目用户
   */
  private Long submitUser;
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