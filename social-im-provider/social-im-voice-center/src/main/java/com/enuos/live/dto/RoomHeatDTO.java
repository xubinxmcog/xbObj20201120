package com.enuos.live.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName RoomHeatDTO
 * @Description: TODO 语音房热度
 * @Author xubin
 * @Date 2020/11/12
 * @Version V2.0
 **/
@Data
public class RoomHeatDTO implements Serializable {

    private static final long serialVersionUID = -3093961184752555522L;
    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * ## 房间贡献榜总贡献
     */
    private Long totalCharmValue;

    /**
     * ## 房间贡献榜本周贡献
     */
    private Long weekCharmValue;

    /**
     * ## 单位时间用户累计赠送礼物贡献
     */
    private Long unitCharmValue;

    /**
     * ## 当前房间内用户数
     */
    private Long roomUserNum;

    /**
     * ## 当前房间内会员用户数
     */
    private Long vipUserNum;

    /**
     * ## 房间关注用户数
     */
    private Long concernNum;

    /**
     *  ## 本周新增关注用户数
     */
    private Long weekConcernNum;

    /**
     * ## 用户累计发送消息数
     */
    private Long activityNum;

    /**
     * ## 语音房累计播放时长 分钟
     */
    private Double totalBroadcastTime;

    /**
     * ## 语音房本周播放时长 分钟
     */
    private Double weekBroadcastTime;

    /**
     * ## 语音房本次开播时长 分钟
     */
    private Double concernBroadcastTime;

    /**
     * 后台推荐值
     */
    private Long headValue;
}
