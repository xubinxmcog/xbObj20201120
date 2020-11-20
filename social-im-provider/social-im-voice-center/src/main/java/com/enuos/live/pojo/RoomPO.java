package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description 语音房
 * @Author wangyingjie
 * @Date 10:51 2020/5/11
 * @Modified
 */
@Data
public class RoomPO extends Page implements Serializable {

    private static final long serialVersionUID = -1669164915587188313L;

    /** 房间号 */
    private Long roomId;

    /** 房主 */
    @NotNull(message = "房主不能为空")
    private Long userId;

    /** 房间名称 */
    @NotBlank(message = "房间名称不能为空")
    private String name;

    /** 房间密码 */
    private String password;

    /** 房间封面 */
    @NotBlank(message = "封面不能为空")
    private String coverUrl;

    /** 房间背景 */
    private String backgroundUrl;

    /** 房间背景id */
    private Integer backgroundId;

    /** 音乐 */
    private String musicUrl;

    /** 主题[0 无 1 脱口秀 2 音乐 3 游戏 4 闲聊 5 情感 6 读文]*/
    @NotNull(message = "主题不能为空")
    private Integer themeId;

    /** 话题 */
    private String topic;

    /** 公告 */
    private String notice;

    /** 位置[0. 4个 1. 8个] */
    @NotNull(message = "房间位置不能为空")
    private Integer seatType;

    /** 开播 */
    private Integer isBroadcast;

    /** 是否推送给好友[0 否 1 是] */
    @NotNull(message = "推送不能为空")
    private Integer isPush;

    /** 状态 */
    private Integer status;

    /** [OTHER] */
    /** 对方ID */
    private Long targetId;

    /** 排麦模式 0:自由模式 1:排麦模式 */
    private Integer micMode;

    /** 公屏 0:启用 1:禁用 */
    private Integer isBanScreen;

    private Integer parentId;

    /** 直播开始时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    public void setPassword(String password) {
        this.password = password;
        /**
        try {
            this.password = StringUtils.isNotBlank(password) ? MD5Utils.encrypt8(password) : password;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        */
    }

}
