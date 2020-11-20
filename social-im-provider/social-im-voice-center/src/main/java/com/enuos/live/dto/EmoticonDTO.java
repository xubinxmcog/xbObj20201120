package com.enuos.live.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @ClassName GiftGiveDTO
 * @Description: TODO 发送表情包入参
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
@Data
@ToString
public class EmoticonDTO {

    /**
     * 发送人ID
     */
    @NotNull(message = "送礼人ID不能为空")
    private Long userId;

    /**
     * 接收人id
     */
    @NotNull(message = "收礼人ID不能为空")
    private Long receiveUserId;

    /**
     * 表情ID
     */
    @NotNull(message = "礼物ID不能为空")
    private Long emId;

    /**
     * 数量
     */
//    @NotNull(message = "礼物数量不能为空")
    private Integer emNum = 1;

    /**
     * 表情名称
     */
    private String emName;

    private String emUrl;

}
