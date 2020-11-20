package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName PicInfo
 * @Description: TODO 图片信息实体类
 * @Author xubin
 * @Date 2020/4/22
 * @Version V1.0
 **/
@Data
public class PicInfo implements Serializable {

  /**
   * 商品图片ID
   */
  private Long id;

  /**
   * 图片URL
   */
  private String picUrl;

  /**
   * 图片原名
   */
  private String picName;

  /**
   * 保存后图片名
   */
  private String picNewName;

  /**
   * 文件类型
   */
  private String picType;

  /**
   * 最后修改时间
   */
  @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date modifiedTime;

  private static final long serialVersionUID = 1L;
}