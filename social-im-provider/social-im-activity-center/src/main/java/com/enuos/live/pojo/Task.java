package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 任务
 * @Author wangyingjie
 * @Date 2020/10/12
 * @Modified
 */
@Data
public class Task implements Serializable {

    private static final long serialVersionUID = -3481798669525393289L;

    /** 任务编码 */
    private String taskCode;

    /** 模板编码 */
    private String templateCode;

    /** 名称 */
    private String name;

    /** 图标 */
    private String iconUrl;

    /** 背景 */
    private String backgroundUrl;

    /** 去完成 */
    private String toFinishPath;

    /** 积分 */
    private Integer integral;

    /** 周次数 */
    private Integer weekCount;

    /** 天次数 */
    private Integer dayCount;

    /** 周进度 */
    private Integer weekProgress;

    /** 周积分 */
    private Integer weekIntegral;

    /** 是否获取[0：否；1：是] */
    private Integer isGot;

}
