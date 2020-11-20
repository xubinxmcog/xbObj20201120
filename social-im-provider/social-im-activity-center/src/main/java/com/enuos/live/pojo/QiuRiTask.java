package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/8/12
 * @Modified
 */
@Data
public class QiuRiTask implements Serializable {

    private static final long serialVersionUID = -1744505122190192656L;

    /** 活动期间日常任务code */
    private String code;

    /** 活动期间日常任务名称 */
    private String title;

    /** 任务奖励 */
    private String rewardCode;

    /** 任务奖励图标 */
    private String url;

    /** 任务奖励数量 */
    private String number;

    /** 去完成 */
    private String toFinishPath;

    /** 是否领取[-1 不可领 0 可领 1 已领取] */
    private Integer isGot;

}
