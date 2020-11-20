package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 房间角色
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
@Data
public class RolePO extends Base implements Serializable {

    private static final long serialVersionUID = 7953919546472812939L;

    /** 房间ID */
    private Long roomId;

    /** 目标用户ID */
    public Long targetUserId;

    /** 角色 */
    private Integer role;

    /** 操作类型 0:添加 1:删除 */
    private Integer type;

}
