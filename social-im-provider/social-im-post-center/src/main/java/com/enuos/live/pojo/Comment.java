package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description 评论
 * @Author wangyingjie
 * @Date 16:03 2020/4/24
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment extends Page implements Serializable {

    private static final long serialVersionUID = 8732130957892439654L;

    /** 主键 */
    private Integer id;

    /** 评论or回复[1 评论 2 回复] */
    private Integer type;

    /** 动态ID */
    private Integer postId;

    /** 评论发布人 */
    private Long toUserId;

    /** 头像 */
    private String iconUrl;

    /** 头像缩略图 */
    private String thumbIconUrl;

    /** 昵称 */
    private String nickName;

    /** 用户性别 [1男 2女] */
    private Integer sex;

    /** 是否好友[0:否;1:是] */
    private Integer isFriend;

    /** 备注 */
    private String remark;

    /** 评论ID */
    private Integer commentId;

    /** 内容 */
    private String content;

    /** 回复标记 */
    private Integer replyFlag;

    /** 评论时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 点赞数 */
    private Integer praiseNum;

    /** 是否点赞[0:否;1:是] */
    private Integer isPraise;

    /** 回复数 */
    private Integer replyNum;

    /** 第一回复 */
    private String replyNickName;

    /** 第一回复 */
    private String replyRemark;

    /** 第一回复内容 */
    private String replyContent;

}
