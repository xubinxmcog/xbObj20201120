package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName ProductCategory
 * @Description: TODO 礼物实体类
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
@Data
public class Gift implements Serializable {
    private Long giftId;

    /**
     * 礼物名称
     */
    private String giftName;

    /**
     * 礼物价格
     */
    private Long giftPrice;

    /**
     * 价格类型id
     */
    private Integer priceType;

    /**
     * 经验值
     */
    private Long expValue;

    /**
     * 类别: 0:礼物 1:券
     */
    private Integer giftType;

    /**
     * 礼物图片链接
     */
    private String giftUrl;

    /**
     * 礼物特效链接
     */
    private String dynamicPicture;

    /**
     * 状态 0:无效 1:有效
     */
    private Integer isStatus;

    /**
     * 贡献/魅力
     */
    private Long charmValue;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}