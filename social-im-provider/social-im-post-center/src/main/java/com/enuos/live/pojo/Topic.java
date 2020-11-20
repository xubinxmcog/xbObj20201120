package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 话题
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
@Data
public class Topic extends Page implements Serializable {

    private static final long serialVersionUID = 778173658477442565L;

    /** 话题ID */
    private Integer topicId;

    /** 话题名称 */
    private String topicName;

    /** 话题描述 */
    private String description;

    /** url */
    private String url;

    /** 参与人数 */
    private Integer joinNum;

    /** 动态数 */
    private Integer postNum;

    /** 是否热门 */
    private Integer isHot;

}
