package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * gift_extra_reward
 * @author 
 */
@Data
public class GiftExtraReward implements Serializable {
    private Long id;

    /**
     * 礼物ID
     */
    private Long giftId;

    /**
     * 奖品code
     */
    private String awardCode;

    /**
     * 奖品有效期限,指定天数 示例 -1：永久 7：7天
     */
    private Integer timeLimit;

    /**
     * 奖品数量ID
     */
    private Integer awardNumId;

    /**
     * 中奖概率 单位%
     */
    private Double probability;

    /**
     * 最后修改时间
     */
//    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}