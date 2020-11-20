package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description 动态评论
 * @Author wangyingjie
 * @Date 2020/8/17
 * @Modified
 */
@Data
public class PostComment extends Base implements Serializable {

    private static final long serialVersionUID = -3127462494148993607L;

    /** 评论ID */
    private Integer id;

    /** 动态ID */
    private Integer postId;

    /** 内容 */
    private String content;

    /** 点赞数 */
    private Integer praiseNum;

    /** 评论时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 评论人ID */
    private Long userId;

    /** 评论人昵称 */
    private String nickName;

    /** 评论备注 */
    private String remark;

}
