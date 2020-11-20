package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName productBackpackTDO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/2
 * @Version V2.0
 **/
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductBackpackDTO implements Serializable{

    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 物品ID
     */
    private Long productId;

    /**
     * 物品code
     */
    private String productCode;

    /**
     * 物品名称
     */
    private String productName;

    /**
     * 物品数量
     */
    private Integer productNum;

    /**
     * 物品图片链接
     */
    private String picUrl;

    /**
     * 有效期
     */
    private Long timeLimit;

    /**
     * 属性
     */
    private String attribute;

    /**
     * 物品描述
     */
    private String descript;

    /**
     * 状态
     */
    private Integer productStatus;

    /**
     * 有效期描述
     */
    private String termDescribe;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date useTime;

    private static final long serialVersionUID = 1L;
}
