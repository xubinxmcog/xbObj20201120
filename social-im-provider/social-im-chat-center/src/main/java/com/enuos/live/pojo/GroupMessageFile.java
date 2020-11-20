package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author WangCaiWen Created on 2020/5/4 15:36
 */
@Data
public class GroupMessageFile implements Serializable {

  private static final long serialVersionUID = -580845044820045802L;
  /**
   * 主键ID
   */
  private Long id;
  /**
   * 群聊ID
   */
  private Long groupId;
  /**
   * 记录ID
   */
  private Long recordId;
  /**
   * 文件类型 [ 0 图片 1 语音 2 视频 ]
   */
  private Integer fileType;
  /**
   * 文件时长 [ 秒 ]
   */
  private Integer fileDuration;
  /**
   * 文件宽度 [ 像素 ]
   */
  private Integer fileWidth;
  /**
   * 文件高度 [ 像素 ]
   */
  private Integer fileHeight;
  /**
   * 访问地址
   */
  private String fileUrl;
  /**
   * 封面访问地址 [视频封面]
   */
  private String fileCoverUrl;
}
