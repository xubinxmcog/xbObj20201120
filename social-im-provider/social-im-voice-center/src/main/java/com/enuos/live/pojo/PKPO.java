package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName PKPO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/7/6
 * @Version V2.0
 **/
@Data
public class PKPO extends Base {

    /**
     * 房间id
     */
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    /**
     * 目标用户id
     */
    private Long targetUserId;

    /**
     * 目标用户id1
     */
    @NotNull(message = "一号PK用户不能为空")
    private Long targetUserId1;

    /**
     * 目标用户id1
     */
    @NotNull(message = "二号PK用户不能为空")
    private Long targetUserId2;

    /**
     * PK 类型
     * 1:魅力 2:人气
     */
    @NotNull(message = "PK 类型不能为空")
    private Integer type;

    /**
     * PK 时间 秒
     */
    @NotNull(message = "PK 时间不能为空")
    private Long time;

    /**
     * PK 票数
     */
    private Integer poll;

}
