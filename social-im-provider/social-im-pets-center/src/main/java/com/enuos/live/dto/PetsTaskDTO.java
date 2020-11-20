package com.enuos.live.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName PetsTaskDTO
 * @Description: TODO 任务列表
 * @Author xubin
 * @Date 2020/10/23
 * @Version V2.0
 **/
@Data
public class PetsTaskDTO {

    private Integer id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务目标值
     */
    private Integer targetValue;

    /**
     * 任务类型 1:每日任务 2:节日任务 3:其他
     */
    private Integer taskType;

    /**
     * 任务描述
     */
    private String describe;

    /**
     * 任务图片
     */
    private String taskPic;

    /**
     * 任务奖励ID
     */
    private Integer rewardId;

    /**
     * 任务已完成值
     */
    private Integer finishValue;

    /**
     * 是否完成 0:未完成 1:已完成
     */
    private Integer isFinish;

    /**
     * 是否领取奖励 0:未领取 1:已领取
     */
    private Integer isReceive;

    private List<PetsRewardDTO> rewards;

}
