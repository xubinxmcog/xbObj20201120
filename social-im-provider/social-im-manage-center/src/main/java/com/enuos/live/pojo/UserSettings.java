package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * user_settings
 * @author 
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserSettings implements Serializable {
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 不接收通知： 0：关 1：开
     */
    private Integer notReceiveNotice;

    /**
     * 聊天消息通知： 0：关 1：开
     */
    private Integer chatNotice;

    /**
     * 系统消息通知： 0：关 1：开
     */
    private Integer systemNotice;

    /**
     * 语音房开播通知： 0：关 1：开
     */
    private Integer roomNotice;

    /**
     * 声音： 0：关 1：开
     */
    private Integer sound;

    /**
     * 震动： 0：关 1：开
     */
    private Integer shock;

    /**
     * 不显示个人位置： 0：关 1：开
     */
    private Integer position;

    /**
     * 不出现好友推荐中：0：关 1：开
     */
    private Integer friendRecommend;

    /**
     * 青少年模式：0：关 1：开
     */
    private Integer teensModel;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}