package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * pets_user_dynamic_msg
 * @author 
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsUserDynamicMsg implements Serializable {
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long gfUserId;

    /**
     * 好友昵称
     */
    private String gfNickName;

    /**
     * 消息
     */
    private String message;

    /**
     * 消息类别
     */
    private String msgType;

    /**
     * 是否已读 0:未读 1:已读
     */
    private Integer isRead;

    /**
     * 录入时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "MM/dd HH:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date indateTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}