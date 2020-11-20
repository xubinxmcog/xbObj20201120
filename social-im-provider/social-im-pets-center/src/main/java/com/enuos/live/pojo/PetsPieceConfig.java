package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * pets_piece_config
 * @author 
 */
@Data
public class PetsPieceConfig implements Serializable {
    private Integer id;

    /**
     * 编码
     */
    private String productCode;

    /**
     * 名字
     */
    private String petsName;

    /**
     * 图片
     */
    private String picUrl;

    /**
     * 参照
     */
    private String referTo;

    /**
     * 所需碎片类型
     */
    private String pieceType;

    /**
     * 碎片数量
     */
    private Integer pieceNum;

    /**
     * 初始性别
     */
    private Byte initSex;

    /**
     * 品质 1:优秀 2:稀有 3:传说 4:神话
     */
    private Integer quality;

    /**
     * 其他属性
     */
    private String attribute;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;

    private static final long serialVersionUID = 1L;
}