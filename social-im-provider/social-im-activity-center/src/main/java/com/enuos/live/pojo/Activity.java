package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 活动中心
 * @Author wangyingjie
 * @Date 2020/8/12
 * @Modified
 */
@Data
public class Activity extends Base implements Serializable {

    private static final long serialVersionUID = -198749588407866330L;

    /** 活动code */
    protected String code;

    /** 活动名称 */
    protected String title;

    /** 活动背景 */
    protected String backgroundUrl;

    /** 活动起始时间 */
    protected String startTime;

    /** 活动结束时间 */
    protected String endTime;

}
