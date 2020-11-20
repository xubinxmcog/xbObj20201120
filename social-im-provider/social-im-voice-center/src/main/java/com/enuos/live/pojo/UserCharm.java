package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName ProductCategory
 * @Description: TODO 用户魅力值实体类
 * @Author xubin
 * @Date 2020/6/18
 * @Version V2.0
 **/
@Data
public class UserCharm implements Serializable {
    private Long charmId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 送礼人ID
     */
    private Long giveUserId;

    /**
     * 礼物数量
     */
    private Integer giftNum;

    /**
     * 礼物ID
     */
    private Long giftId;

    /**
     * 魅力值
     */
    private Long charmValue;

    /**
     * 礼物价格
     */
    private Long giftPrice;

    /**
     * 价格类型id
     */
    private Integer priceType;

    /**
     * 房间id
     */
    private Long roomId;

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