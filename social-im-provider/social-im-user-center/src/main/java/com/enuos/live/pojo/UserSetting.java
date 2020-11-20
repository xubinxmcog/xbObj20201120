package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * user_settings
 * @author 
 */
@Data
@ToString
public class UserSetting implements Serializable {
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 不接收通知： 0：关 1：开
     */
    private Byte notReceiveNotice;

    /**
     * 聊天消息通知： 0：关 1：开
     */
    private Byte chatNotice;

    /**
     * 系统消息通知： 0：关 1：开
     */
    private Byte systemNotice;

    /**
     * 语音房开播通知： 0：关 1：开
     */
    private Byte roomNotice;

    /**
     * 声音： 0：关 1：开
     */
    private Byte sound;

    /**
     * 震动： 0：关 1：开
     */
    private Byte shock;

    /**
     * 不显示个人位置： 0：关 1：开
     */
    private Byte position;

    /**
     * 不出现好友推荐中：0：关 1：开
     */
    private Byte friendRecommend;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}