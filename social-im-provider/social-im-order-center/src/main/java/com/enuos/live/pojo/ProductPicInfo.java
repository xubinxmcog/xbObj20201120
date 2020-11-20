package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * product_pic_info
 *
 * @author
 */
@Data
public class ProductPicInfo implements Serializable {
    /**
     * 商品图片ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 图片描述
     */
    private String picDesc;

    /**
     * 图片URL
     */
    private String picUrl;

    /**
     * 原文件名
     */
    private String picName;

    /**
     * 保存文件名
     */
    private String picNewName;

    /**
     * 文件类型
     */
    private String picType;

    /**
     * 是否主图：0.非主图1.主图
     */
    private Byte isMaster;

    /**
     * 图片排序
     */
    private Byte picOrder;

    /**
     * 图片是否有效：0无效 1有效
     */
    private Byte picStatus;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}