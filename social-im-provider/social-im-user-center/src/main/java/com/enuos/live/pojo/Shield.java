package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 屏蔽用户或者动态
 * @Author wangyingjie
 * @Date 2020/6/1
 * @Modified
 */
@Data
public class Shield extends Base implements Serializable {

    private static final long serialVersionUID = -514432171114158535L;

    /** 目标用户ID */
    private Long toUserId;

    /** 动态ID */
    private Integer postId;

}
