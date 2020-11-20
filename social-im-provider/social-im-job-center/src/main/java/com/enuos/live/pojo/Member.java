package com.enuos.live.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/7/2
 * @Modified
 */
@Data
public class Member {

    /** 用户ID */
    private Long userId;

    /** VIP等级 */
    private Integer vip;

    /** 成长值 */
    private Integer growth;

    /** 到期时间 */
    private LocalDateTime expirationTime;

    /** 扭蛋数 */
    private Integer gashapon;

}
