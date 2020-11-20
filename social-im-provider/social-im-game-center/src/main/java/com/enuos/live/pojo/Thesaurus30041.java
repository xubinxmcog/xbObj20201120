package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;
import lombok.Data;

/**
 * TODO 你画我猜(Thesaurus)实体类
 *
 * @author wangcaiwen
 * @version 2.0
 * @since 2020-07-16 15:19:17
 */

@Data
public class Thesaurus30041 implements Serializable {

  private static final long serialVersionUID = 333067725664402165L;
  /**
   * 主键ID
   */
  private Integer id;
  /**
   * 词汇
   */
  private String lexicon;
  /**
   * 词汇提示
   */
  private String lexiconHint;
  /**
   * 词汇字数【1 一字数  2 二字数 3 三字数 4 四字数 5 五字数 6 六字数 7 七字数 9 九字数  10 十字数】
   */
  private Integer lexiconWords;
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