package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TODO 互动通知.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/4/27 13:18
 */

@Data
public class NoticeInteract implements Serializable {
  private static final long serialVersionUID = -839015756922073594L;
  /** 主键ID. */
  private Long id;
  /** 发起人ID. */
  private Long sponsorId;
  /** 接收人ID. */
  private Long receiverId;
  /** 动态ID. */
  private Integer storyId;
  /** 附属ID(即评论ID). */
  private Integer attachId;
  /** 阅读状态 0-未读 1-已读. */
  private Integer noticeRead;
  /** 通知来源 0-关注用户 1-评论动态 2-回复评论 3-转发动态 4-点赞动态 5-点赞评论 6-@xxx用户. */
  private Integer noticeSource;
  /** 创建时间. */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
  /** 更新时间. */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updateTime;
}
