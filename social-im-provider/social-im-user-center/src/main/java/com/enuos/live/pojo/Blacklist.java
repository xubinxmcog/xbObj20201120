package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 黑名单
 * @Author wangyingjie
 * @Date 9:47 2020/4/24
 * @Modified
 */
@Data
public class Blacklist extends Base implements Serializable {

    private static final long serialVersionUID = -7984825314673503100L;

    /** 主键 */
    private Integer id;

    /** 目标用户ID */
    private Long toUserId;

    /** 黑名单评级[0：拉黑；1：不看动态] */
    private Integer rating;

}
