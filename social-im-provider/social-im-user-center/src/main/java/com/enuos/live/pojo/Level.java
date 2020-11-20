package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 等级
 * @Author wangyingjie
 * @Date 2020/7/21
 * @Modified
 */
@Data
public class Level extends Base implements Serializable {

    private static final long serialVersionUID = 4259833005062356772L;

    /** 等级 */
    private Integer level;

}
