package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 令状奖励
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Data
public class WritReward implements Serializable {

    private static final long serialVersionUID = -5923841174192424295L;

    /** 等级 */
    private Integer level;

    /** 基础奖励 */
    private Reward baseReward;

    /** 进阶奖励 */
    private Reward stepReward;

}
