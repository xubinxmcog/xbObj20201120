package com.enuos.live.pojo;

import lombok.Data;

/**
 * @ClassName TaskReward
 * @Description: TODO 奖励配置明细
 * @Author xubin
 * @Date 2020/9/3
 * @Version V2.0
 **/
@Data
public class TaskReward {

    private Integer id;

    /**
     * 任务code
     */
    private String taskCode;

    /**
     * 奖品CODE
     */
    private String rewardCode;

    /**
     * 期限[单位：s]
     */
    private Long life;

    /**
     * 奖品数量
     */
    private String number;

    /**
     * 等级
     */
    private Integer suffix;
}
