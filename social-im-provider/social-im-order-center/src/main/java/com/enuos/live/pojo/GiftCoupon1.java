package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName ProductCategory
 * @Description: TODO 礼物券实体类
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
@Data
public class GiftCoupon1 implements Serializable {
    private Long couponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 礼物券id
     */
    private String giftCouponId;

    /**
     * 期限[单位：s]
     */
    private Long life;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}