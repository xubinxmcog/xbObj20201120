package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName RobVO
 * @Description: TODO 抢红包入参
 * @Author xubin
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Data
public class RobVO {
    /**
     * 红包ID
     */
    @NotNull(message = "红包ID不能为空")
    private Long rpId;

    /**
     * 发红包人ID
     */
    @NotNull(message = "发红包人ID不能为空")
    private Long sendUserId;

    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    /**
     * 抢红包人ID
     */
    @NotNull(message = "抢红包人ID不能为空")
    private Long userId;
}
