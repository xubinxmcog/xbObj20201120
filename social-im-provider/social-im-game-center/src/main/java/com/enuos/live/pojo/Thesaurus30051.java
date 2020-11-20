package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;
import lombok.Data;

/**
 * TODO 谁是卧底(Thesaurus)实体类
 *
 * @author wangcaiwen
 * @version 2.0
 * @since 2020-07-16 15:19:57
 */

@Data
public class Thesaurus30051 implements Serializable {

  private static final long serialVersionUID = 581467907446433206L;
  /**
   * 主键ID
   */
  private Integer id;
  /**
   * 平民词汇
   */
  private String lexiconMass;
  /**
   * 卧底词汇
   */
  private String lexiconSpy;
  /**
   * 激活状态【0 未激活  1 已激活】
   */
  private Integer lexiconStatus;
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
   * 提交词汇用户
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