package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author WangCaiWen Created on 2020/4/9 16:55
 */
@Data
public class Group implements Serializable {

  private static final long serialVersionUID = 140418131478984488L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 群聊头像
   */
  private String groupIcon;
  /**
   * 群聊ID
   */
  private Long groupId;
  /**
   * 群主ID
   */
  private Long groupAdmin;
  /**
   * 群聊名称
   */
  private String groupName;
  /**
   * 群聊公告
   */
  private String groupNotice;
  /**
   * 群聊简介
   */
  private String groupIntro;
  /**
   * 群聊等级 [0. 50人 1. 100人]
   */
  private Integer groupGrade;
  /**
   * 群聊人数
   */
  private Integer groupNum;
  /**
   * 群聊状态 [0 正常 1 警告中 2 已封禁]
   */
  private Integer groupStatus;
  /**
   * 群聊警告次数
   */
  private Integer groupWarn;
  /**
   * 自动更新 [0 开启 1关闭]
   */
  private Integer groupAuto;
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
