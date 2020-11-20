package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 点赞
 * @Author wangyingjie
 * @Date 13:31 2020/4/26
 * @Modified
 */
@Data
public class Praise extends Base implements Serializable {

    private static final long serialVersionUID = 1880735402774233127L;

    /** 动态ID */
    private Integer postId;

    /** 评论ID */
    private Integer commentId;

    /** 回复ID */
    private Integer replyId;

    /** 用户ID */
    private Long toUserId;

    /** 点赞或取消点赞[1:点赞；0：取消赞] */
    private Integer giveOrCancel;

    /** 点赞类型[0:动态点赞；1：评论点赞；2：回复点赞] */
    private Integer type;
}
