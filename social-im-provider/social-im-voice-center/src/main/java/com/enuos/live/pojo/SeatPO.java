package com.enuos.live.pojo;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/13
 * @Modified
 */
@Data
public class SeatPO extends Base implements Serializable {

    private static final long serialVersionUID = 3493783682672095271L;

    private Integer id;

    /** 房间号 */
    @NotNull(message = "房间号不能为空")
    private Long roomId;

    /** 座号 */
//    @NotNull(message = "座号不能为空")
    private Integer seatId;

    private Long targetUserId;

    /** 昵称 */
    private String nickName;

    /** 用户信息 */
    private String thumbIconUrl;

    /** 性别 */
    private Integer sex;

    /** 是否锁 0 正常 1 锁*/
    private Integer isLock;

    /** 是否加入[0 离开 1 加入] */
    private Integer isJoin;

    /** 是否禁麦 0:否 1:是 */
    private Integer mic;

    /** 操作 0:加锁解锁 1:禁麦 2:报人上麦 3:踢人 */
    private Integer type;

}
