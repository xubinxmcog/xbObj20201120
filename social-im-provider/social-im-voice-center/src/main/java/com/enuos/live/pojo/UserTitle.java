package com.enuos.live.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName UserTitle
 * @Description: TODO 用户称号
 * @Author xubin
 * @Date 2020/9/3
 * @Version V2.0
 **/
@Data
public class UserTitle {

    private Integer id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 称号编码
     */
    private String titleCode;

    /**
     * 到期时间
     */
    private Date expireTime;
}
