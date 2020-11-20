package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 注销账户
 * @Author wangyingjie
 * @Date 10:32 2020/5/9
 * @Modified
 */
@Data
public class Logout extends Base implements Serializable {

    private static final long serialVersionUID = 5482826135468362279L;

    /** 用户ID */
    private Long userId;

    /** 注销理由 */
    private String causeIds;

    /** 详细描述 */
    private String description;

}
