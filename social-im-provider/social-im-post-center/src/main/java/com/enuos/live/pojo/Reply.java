package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description 评论回复
 * @Author wangyingjie
 * @Date 15:38 2020/4/24
 * @Modified
 */
@Data
public class Reply extends Base implements Serializable {

    private static final long serialVersionUID = -1510710033533334628L;

    /** ID */
    private Integer id;

    /** 性别 */
    private Integer sex;

    /** 头像 */
    private String iconUrl;

    /** 头像缩略图 */
    private String thumbIconUrl;

    /** 昵称 */
    private String nickName;

    /** 备注 */
    private String remark;

    /** 回复内容 */
    private String content;

    /** 点赞数 */
    private Integer praiseNum;

    /** 是否点赞 */
    private Integer isPraise;

    /** 回复时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
