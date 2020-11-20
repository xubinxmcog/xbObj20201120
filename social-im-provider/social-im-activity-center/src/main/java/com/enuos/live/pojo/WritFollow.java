package com.enuos.live.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/10/12
 * @Modified
 */
@Getter
@Setter
public class WritFollow {

    /** 用户ID */
    private Long userId;

    /** 编码 */
    private String code;

    /** 周进度 */
    private Integer weekProgress;

    /** 周积分 */
    private Integer weekIntegral;

    public WritFollow() {
        super();
    }

    public WritFollow(Long userId, String code, Integer weekProgress, Integer weekIntegral) {
        this.userId = userId;
        this.code = code;
        this.weekProgress = weekProgress;
        this.weekIntegral = weekIntegral;
    }
}
