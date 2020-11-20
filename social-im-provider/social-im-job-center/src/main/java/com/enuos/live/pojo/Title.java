package com.enuos.live.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @Description 用户称号
 * @Author wangyingjie
 * @Date 2020/10/19
 * @Modified
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Title {

    /** 主键 */
    private Integer id;

    /** 用户ID */
    private Long userId;

    /** 称号 */
    private String titleCode;

    /** 到期时间 */
    private LocalDateTime expireTime;

    public Title(Long userId, String titleCode, LocalDateTime expireTime) {
        this.userId = userId;
        this.titleCode = titleCode;
        this.expireTime = expireTime;
    }
}
