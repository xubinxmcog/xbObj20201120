package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/28
 * @Modified
 */
@Data
public class BSGashapon implements Serializable {

    private static final long serialVersionUID = -7806866761535161182L;

    /** 标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 任务编码 */
    private String code;

    /** 任务来源[0：系统预设；1：自设] */
    private Integer source;

    /** 任务图标 */
    private String iconUrl;

    /** 背景 */
    private String backgroundUrl;

    /** 任务源[去完成] */
    private String toFinishPath;

    /** 序号 */
    private Integer sort;

    /** 任务类别[1：每日签到；2：星座签到；3：活跃；4：日常；5：成就；6：扭蛋抽奖；7：扭蛋兑换] */
    @NotNull(message = "类别不能为空")
    private Integer category;

    /** 任务类型 */
    private Integer type;

    /** 任务起始时间 */
    @NotBlank(message = "起始时间不能为空")
    private String startTime;

    /** 任务结束时间 */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /** 间隔时间 */
    @NotNull(message = "间隔时间不能为空")
    private Integer space;

    /** 间隔时间单位[H 小时 M 分钟 S 秒] */
    @NotNull(message = "间隔时间单位不能为空")
    private String spaceUnit;

    /** 消耗 */
    @NotNull(message = "消耗扭蛋不能为空")
    private Integer expend;

    /** 参与次数 */
    @NotNull(message = "参与次数不能为空")
    private Integer joinCount;

    /** 定时任务设置 */
    @Valid
    private BSJob job;

    /** 奖励 */
    @Valid
    private List<BSReward> rewardList;
}

/**
 * @Description reward
 * @Author wangyingjie
 * @Date 2020/6/29
 * @Modified
 */
@Data
class BSReward implements Serializable {

    private static final long serialVersionUID = -2946349864786160240L;

    @NotBlank(message = "奖品不能为空")
    private String rewardCode;

    @NotBlank(message = "奖品数量不能为空")
    private String number;

    @NotNull(message = "后缀不能为空")
    private Integer suffix;
}

/**
 * @Description job
 * @Author wangyingjie
 * @Date 2020/6/29
 * @Modified
 */
@Data
class BSJob implements Serializable {

    private static final long serialVersionUID = -4292374348630028803L;

    /** job表达式 */
    @NotBlank(message = "cron不能为空")
    private String cron;
}