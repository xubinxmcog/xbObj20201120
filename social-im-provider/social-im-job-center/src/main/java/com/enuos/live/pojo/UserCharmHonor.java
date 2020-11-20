package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * user_charm_honor
 * @author 
 */
@Data
public class UserCharmHonor implements Serializable {
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 魅力值
     */
    private Long charmValue;

    private Long totalValue;

    /**
     * 类型 1:魅力 2:守护
     */
    private Integer type;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}