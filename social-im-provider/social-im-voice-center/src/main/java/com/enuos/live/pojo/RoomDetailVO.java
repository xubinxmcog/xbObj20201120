package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/13
 * @Modified
 */
@Data
public class RoomDetailVO extends Base implements Serializable {

    private static final long serialVersionUID = 1279991108829804440L;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 房间名称
     */
    private String name;

    /**
     * 封面
     */
    private String coverUrl;

    /**
     * 背景
     */
    private String backgroundUrl;

    /**
     * 音乐
     */
    private String musicUrl;

    /**
     * 主题
     */
    private Integer themeId;

    /**
     * 主题名称
     */
    private String themeName;

    /**
     * 话题
     */
    private String topic;

    /**
     * 公告
     */
    private String notice;

    /**
     * 是否关注
     */
    private Integer isFollow;

    /**
     * 在线人数
     */
    private Integer onNum;

    /**
     * 是否在座位
     */
    private Integer isOnSeat;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 排麦模式 0:自由模式 1:排麦模式
     */
    private Integer micMode;

    /**
     * 是否启用公屏 0:启用 1:禁用
     */
    private Integer isBanScreen;

    /**
     * 专辑ID
     */
    private Integer albumId;

    /**
     * 专辑标题
     */
    private String albumTitle;

    /**
     * 专辑封面图标
     */
    private String coverUrlSmall;

    /**
     * 直播开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 座位详情
     */
    private List<SeatVO> seatList;

    /**
     * 电台信息
     */
    private VoiceRoomRadioVO radioVO;

    /**
     * 拉流地址
     */
    private String pullUrl;

    /**
     * 背景id
     */
    private Integer backgroundId;

    /**
     * 背景名
     */
    private String backgroundName;

}
