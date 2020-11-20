package com.enuos.live.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @ClassName GiftGiveDTO
 * @Description: TODO 赠送礼物入参
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
@Data
@ToString
public class GiftGiveDTO {

    /**
     * 送礼人ID
     */
    @NotNull(message = "送礼人ID不能为空")
    private Long userId;

    /**
     * 收礼人id
     */
    @NotNull(message = "收礼人ID不能为空")
    private Long receiveUserId;

    /**
     * 礼物ID
     */
    @NotNull(message = "礼物ID不能为空")
    private Long giftId;

    /**
     * 礼物券code
     */
//    @NotNull(message = "礼物券code不能为空")
    private String giftCouponId;

    /**
     * 礼物数量
     */
    @NotNull(message = "礼物数量不能为空")
    @Min(value = 1, message = "礼物数量最少为1")
    private Integer giftNum;

    /**
     * 礼物名称
     */
    private String giftName;

    /**
     * 魅力值
     */
    private Long charmValue;

    /**
     * 房间ID
     */
    private Long roomId = 0L;


}
