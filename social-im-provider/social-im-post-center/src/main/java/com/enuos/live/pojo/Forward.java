package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 转发
 * @Author wangyingjie
 * @Date 9:09 2020/4/27
 * @Modified
 */
@Data
public class Forward extends Base implements Serializable {

    private static final long serialVersionUID = 587913698258458851L;

    /** 性别 */
    private Integer sex;

    /** 头像 */
    private String iconUrl;

    /** 头像缩略图 */
    private String thumbIconUrl;

    /** 昵称 */
    private String nickName;

    /** 是否好友[0：否；1：是] */
    private Integer isFriend;

    /** 好友备注 */
    private String remark;

    /** 账号等级 */
    private Integer level;

    /** 转发时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
