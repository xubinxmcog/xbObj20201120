package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/13
 * @Modified
 */
@Data
public class SeatVO implements Serializable {

    private static final long serialVersionUID = 4185579348289501118L;

    /** 座位号 */
    private Integer seatId;

    /** 用户ID */
    private Long userId;

    /** 昵称 */
    private String nickName;

    /** 头像 */
    private String thumbIconUrl;

    /** 头像框 */
    private String iconFrame;

    /** 性别 */
    private Integer sex;

    /** 是否锁[0 不是 1 是] */
    private Integer isLock;

    /** 是否禁麦 */
    private Integer isBanMic;

    /** 房间对用户：1 房主 2 管理员 */
    private Integer role;

}
