package com.enuos.live.dto;

import lombok.Data;

/**
 * @ClassName PetsRewardDTO
 * @Description: TODO 任务奖励
 * @Author xubin
 * @Date 2020/10/23
 * @Version V2.0
 **/
@Data
public class PetsRewardDTO {

    /**
     * 奖品code
     */
    private String productCode;

    /**
     * 奖品数量
     */
    private Integer num;

    /**
     * 奖品名称
     */
    private String productName;

    /**
     * 奖品图片
     */
    private String picUrl;
}
