package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName RoomSetUp
 * @Description: TODO
 * @Author xubin
 * @Date 2020/6/28
 * @Version V2.0
 **/
@Data
public class RoomSetUp implements Serializable {

    /**
     * 房间号
     */
    @NotNull(message = "房间号不能为空")
    private Long roomId;

    /**
     * 操作类型 :
     */
    private Integer type;
}
