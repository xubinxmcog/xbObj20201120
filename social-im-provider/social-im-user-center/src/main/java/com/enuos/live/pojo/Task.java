package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 任务
 * @Author wangyingjie
 * @Date 2020/6/11
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task extends Page implements Serializable {

    private static final long serialVersionUID = -8208500910533948901L;

    /** 任务标题 */
    private String title;

    /** 任务code */
    private String code;

    /** 任务描述 */
    private String description;

    /** 背景 */
    private String backgroundUrl;

    /** 任务源[去完成] */
    private String toFinishPath;

    /** 任务类型 */
    private String taskCode;

    /** 类型 */
    private Integer groupId;

    /** 后缀 */
    private Integer suffix;

    /** 是否获得[-1 不可获取 0 可获得 1 已获得] */
    private Integer isGot;

    /** 参与次数 */
    private Integer joinCount;

}
